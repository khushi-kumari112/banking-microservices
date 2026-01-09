package com.banking.loan_service.util;

import com.banking.loan_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.success(message, data);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, String details, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.error(message, details);
        return ResponseEntity.status(status).body(response);
    }
}