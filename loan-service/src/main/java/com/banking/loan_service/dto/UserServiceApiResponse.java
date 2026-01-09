package com.banking.loan_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserServiceApiResponse<T> {
    private LocalDateTime timestamp;
    private String status;
    private String message;
    private T data;

    // Map 'status' to 'success' for compatibility
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}