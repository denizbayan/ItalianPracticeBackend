package com.itpractice.backend.auth.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.itpractice.backend.auth.dto.*;
import com.itpractice.backend.auth.entity.AuthProvider;
import com.itpractice.backend.auth.entity.User;
import com.itpractice.backend.auth.repository.UserRepository;
import com.itpractice.backend.common.exception.ApiException;
import com.itpractice.backend.common.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration RESET_CODE_TTL = Duration.ofMinutes(15);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${app.env:development}")
    private String appEnv;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .authProvider(AuthProvider.EMAIL)
                .build();

        user = userRepository.save(user);
        return issueTokensAndBuildResponse(user, "User registered successfully");
    }

    @Transactional
    public AuthResponse signin(SigninRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return issueTokensAndBuildResponse(user, "Signin successful");
    }

    @Transactional
    public AuthResponse socialLogin(SocialSigninRequest request) {
        AuthProvider provider = AuthProvider.valueOf(request.provider().trim().toUpperCase());
        SocialIdentity identity = resolveSocialIdentity(request);

        User user = userRepository.findByAuthProviderAndProviderId(provider, identity.providerId())
                .or(() -> identity.email() != null ? userRepository.findByEmail(identity.email()) : java.util.Optional.empty())
                .orElseGet(() -> User.builder()
                        .username(generateUniqueUsername(request.username(), identity.email(), provider))
                        .email(identity.email() != null ? identity.email() : identity.providerId() + "@social.placeholder")
                        .authProvider(provider)
                        .providerId(identity.providerId())
                        .build());

        if (user.getProviderId() == null) {
            user.setAuthProvider(provider);
            user.setProviderId(identity.providerId());
        }

        user = userRepository.save(user);
        return issueTokensAndBuildResponse(user, "Social login successful");
    }

    @Transactional
    public AuthResponse refreshTokens(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtService.validateRefreshToken(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Session is no longer active"));

        String incomingTokenHash = hashToken(request.refreshToken());
        if (user.getRefreshTokenHash() == null || !MessageDigest.isEqual(
                incomingTokenHash.getBytes(StandardCharsets.UTF_8),
                user.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        return issueTokensAndBuildResponse(user, "Token refreshed successfully");
    }

    @Transactional
    public MessageResponse logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRefreshTokenHash(null);
        userRepository.save(user);

        return MessageResponse.of("Logged out successfully.");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null) {
            return MessageResponse.of("If the email exists, a reset code has been sent.");
        }

        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        user.setResetPasswordCodeHash(passwordEncoder.encode(code));
        user.setResetPasswordExpiresAt(Instant.now().plus(RESET_CODE_TTL));
        userRepository.save(user);

        String debugCode = !"production".equalsIgnoreCase(appEnv) ? code : null;
        return new MessageResponse("If the email exists, a reset code has been sent.", null, debugCode);
    }

    @Transactional(readOnly = true)
    public MessageResponse verifyResetCode(VerifyResetCodeRequest request) {
        User user = getValidResetUser(request.email(), request.code());
        return new MessageResponse("Reset code verified successfully.", user != null, null);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = getValidResetUser(request.email(), request.code());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordCodeHash(null);
        user.setResetPasswordExpiresAt(null);
        user.setRefreshTokenHash(null);
        userRepository.save(user);

        return MessageResponse.of("Password has been successfully reset.");
    }

    @Transactional
    public MessageResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getPasswordHash() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password change is not available for social login accounts");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return MessageResponse.of("Password updated successfully.");
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public MessageResponse deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
        return MessageResponse.of("Account successfully deleted.");
    }

    private User getValidResetUser(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code"));

        if (user.getResetPasswordCodeHash() == null
                || user.getResetPasswordExpiresAt() == null
                || user.getResetPasswordExpiresAt().isBefore(Instant.now())
                || !passwordEncoder.matches(code, user.getResetPasswordCodeHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code");
        }
        return user;
    }

    private AuthResponse issueTokensAndBuildResponse(User user, String message) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        // Store SHA-256 hash of the refresh token
        user.setRefreshTokenHash(hashToken(refreshToken));
        userRepository.save(user);

        return new AuthResponse(
                message,
                accessToken,
                refreshToken,
                UserResponse.from(user)
        );
    }

    private String generateUniqueUsername(String requestedUsername, String email, AuthProvider provider) {
        String base = null;
        if (requestedUsername != null && !requestedUsername.isBlank()) {
            base = requestedUsername.trim().replaceAll("[^a-zA-Z0-9_.]", "");
        } else if (email != null && email.contains("@")) {
            base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_.]", "");
        }

        if (base == null || base.length() < 3) {
            base = provider.name().toLowerCase() + "_user";
        }
        if (base.length() > 22) {
            base = base.substring(0, 22);
        }

        String candidate = base;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + String.format("%04d", SECURE_RANDOM.nextInt(10_000));
        }
        return candidate;
    }

    private SocialIdentity resolveSocialIdentity(SocialSigninRequest request) {
        if (request.providerId() != null && !request.providerId().isBlank()) {
            String email = request.email() != null ? request.email().trim().toLowerCase() : null;
            return new SocialIdentity(request.providerId().trim(), email);
        }

        if (request.idToken() != null && !request.idToken().isBlank()) {
            try {
                String[] parts = request.idToken().split("\\.");
                if (parts.length >= 2) {
                    byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
                    JsonNode payload = objectMapper.readTree(decodedBytes);
                    if (payload.hasNonNull("sub")) {
                        String sub = payload.get("sub").asText();
                        String email = payload.hasNonNull("email")
                                ? payload.get("email").asText().trim().toLowerCase()
                                : (request.email() != null ? request.email().trim().toLowerCase() : null);
                        return new SocialIdentity(sub, email);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, "Either providerId or a valid idToken must be provided");
    }

    // Hash high-entropy tokens (like JWT refresh tokens) using SHA-256 to avoid BCrypt's 72-byte limit
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private record SocialIdentity(String providerId, String email) {}
}