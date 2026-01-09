package com.banking.account_service.util;

import com.banking.account_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Response Utility
 * Creates standardized API responses
 */
public class ResponseUtil {

    /**
     * Success response with data (returns 200 OK)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Success response with data and custom status
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message,
            T data,
            HttpStatus status
    ) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Success response without data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message,
            HttpStatus status
    ) {
        return success(message, null, status);
    }

    /**
     * Error response
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(
            String message,
            HttpStatus status
    ) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}