

package com.banking.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationResponse {
    private String internalUserId;
    private String email;
    private String phone;
    private String fullName;
    private String kycStatus;
    private boolean exists;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean kycVerified;
    private boolean active;
}