package com.jason7599.postnaut.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
    public UserLoginRequest {
        if (username != null) {
            username = username.strip();
        }
    }
}
