package com.jason7599.postnaut.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ApiErrorResponse(e.getErrorCode()));
    }

    // Bean validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ApiErrorCode.VALIDATION_ERROR.getMessage());

        return ResponseEntity
                .status(ApiErrorCode.VALIDATION_ERROR.getStatus())
                .body(new ApiErrorResponse(
                        ApiErrorCode.VALIDATION_ERROR,
                        message
                ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);

        return ResponseEntity
                .status(ApiErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ApiErrorResponse(ApiErrorCode.INTERNAL_SERVER_ERROR));
    }
}
