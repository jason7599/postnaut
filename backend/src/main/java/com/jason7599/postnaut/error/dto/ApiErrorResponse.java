package com.jason7599.postnaut.error.dto;

import com.jason7599.postnaut.error.ApiErrorCode;

public record ApiErrorResponse(
        ApiErrorCode code,
        String message
) {
    public ApiErrorResponse(ApiErrorCode code) {
        this(code, code.getMessage());
    }
}
