
package com.banking.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin Registration Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegistrationResponse {
    private String internalUserId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String message;
}