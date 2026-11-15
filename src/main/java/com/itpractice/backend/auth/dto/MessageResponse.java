package com.itpractice.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(
        String message,
        Boolean valid,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String debugResetToken
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message, null, null);
    }
}