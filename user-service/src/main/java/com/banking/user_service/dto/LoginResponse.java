package com.banking.user_service.dto;

import com.banking.user_service.enums.KycStatus;
import com.banking.user_service.enums.OnboardingStatus;
import com.banking.user_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String internalUserId;
    private String fullName;
    private Long expiresIn; // milliseconds
}
