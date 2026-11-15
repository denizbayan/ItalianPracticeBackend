package com.itpractice.backend.auth.dto;

import com.itpractice.backend.auth.entity.User;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String authProvider
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAuthProvider().name().toLowerCase()
        );
    }
}