package com.itpractice.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SocialSigninRequest(
        @NotBlank(message = "Provider is required")
        @Pattern(regexp = "(?i)google|apple", message = "Provider must be google or apple")
        String provider,

        String providerId,

        String idToken,

        String email,

        String username
) {}