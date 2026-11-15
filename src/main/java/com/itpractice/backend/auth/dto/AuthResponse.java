package com.itpractice.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        String message,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        UserResponse user
) {}