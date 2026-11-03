package com.jason7599.postnaut.error;

public record ApiErrorResponse(
        ApiErrorCode code,
        String message
) {
    public ApiErrorResponse(ApiErrorCode code) {
        this(code, code.getMessage());
    }
}
