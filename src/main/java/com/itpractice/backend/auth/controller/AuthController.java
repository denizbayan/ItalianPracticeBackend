package com.itpractice.backend.auth.controller;

import com.itpractice.backend.auth.dto.*;
import com.itpractice.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/signin")
    public AuthResponse signin(@Valid @RequestBody SigninRequest request) {
        return authService.signin(request);
    }

    @PostMapping("/social")
    public AuthResponse socialLogin(@Valid @RequestBody SocialSigninRequest request) {
        return authService.socialLogin(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshTokens(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal UUID userId) {
        return authService.logout(userId);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/verify-reset-code")
    public MessageResponse verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        return authService.verifyResetCode(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PatchMapping("/change-password")
    public MessageResponse changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return authService.changePassword(userId, request);
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal UUID userId) {
        return authService.getProfile(userId);
    }

    @DeleteMapping("/account")
    public MessageResponse deleteAccount(@AuthenticationPrincipal UUID userId) {
        return authService.deleteAccount(userId);
    }
}