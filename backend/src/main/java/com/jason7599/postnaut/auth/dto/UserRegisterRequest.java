package com.jason7599.postnaut.auth.dto;

import com.jason7599.postnaut.user.UserConstraints;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotNull(message = "Username is required")
        @Size(
                min = UserConstraints.USERNAME_MIN_LENGTH,
                max = UserConstraints.USERNAME_MAX_LENGTH,
                message = UserConstraints.USERNAME_LENGTH_MESSAGE
        )
        @Pattern(
                regexp = UserConstraints.USERNAME_REGEX,
                message = UserConstraints.USERNAME_REGEX_MESSAGE
        )
        String username,

        @NotNull(message = "Password is required")
        @Size(
                min = UserConstraints.PASSWORD_MIN_LENGTH,
                max = UserConstraints.PASSWORD_MAX_LENGTH,
                message = UserConstraints.PASSWORD_LENGTH_MESSAGE
        )
        String password,

        @NotNull(message = "Display name is required")
        @Size(
                min = UserConstraints.DISPLAY_NAME_MIN_LENGTH,
                max = UserConstraints.DISPLAY_NAME_MAX_LENGTH,
                message = UserConstraints.DISPLAY_NAME_LENGTH_MESSAGE
        )
        @Pattern(
                regexp = UserConstraints.DISPLAY_NAME_REGEX,
                message = UserConstraints.DISPLAY_NAME_MESSAGE
        )
        String displayName
) {
    public UserRegisterRequest {
        if (username != null) {
            username = username.strip();
        }
        if (displayName != null) {
            displayName = displayName.strip();
        }
    }
}