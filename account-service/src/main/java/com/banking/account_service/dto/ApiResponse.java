package com.banking.account_service.dto;  // ✅ Must be in dto package

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper
 *
 * @author Khushi Kumari
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private String status;  // "SUCCESS" or "ERROR"
    private String message;
    private T data;
}
