package com.jason7599.postnaut.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiErrorCode {
    // Server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error"),

    // Bean validation
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST,"Invalid request"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    USERNAME_TAKEN(HttpStatus.CONFLICT, "This username is taken"),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Bad credentials"),
    ;

    private final HttpStatus status;
    private final String message;
}
