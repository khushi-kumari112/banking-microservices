package com.banking.account_service.exception;

import com.banking.account_service.dto.ApiResponse;
import com.banking.account_service.exception.custom.*;
import com.banking.account_service.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler

 * Centralized exception handling for all Account Service exceptions
 * Returns consistent error responses

 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle AccountNotFoundException
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleAccountNotFound(
            AccountNotFoundException ex
    ) {
        log.error("Account not found: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handle InsufficientBalanceException
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficientBalance(
            InsufficientBalanceException ex
    ) {
        log.error("Insufficient balance: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle InvalidAccountOperationException
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(InvalidAccountOperationException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidOperation(
            InvalidAccountOperationException ex
    ) {
        log.error("Invalid account operation: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handle AccountAlreadyExistsException
     * HTTP 409 - Conflict
     */
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleAccountAlreadyExists(
            AccountAlreadyExistsException ex
    ) {
        log.error("Account already exists: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handle UserNotFoundException (from User Service)
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFound(
            UserNotFoundException ex
    ) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handle UserServiceUnavailableException
     * HTTP 503 - Service Unavailable
     */
    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<String>> handleUserServiceUnavailable(
            UserServiceUnavailableException ex
    ) {
        log.error("User Service unavailable: {}", ex.getMessage());
        return ResponseUtil.error(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Handle Validation Errors
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation errors: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .status("error")
                        .message("Validation failed")
                        .data(errors)
                        .build()
                );
    }

    /**
     * Handle Generic Exceptions
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(
            Exception ex
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseUtil.error(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
