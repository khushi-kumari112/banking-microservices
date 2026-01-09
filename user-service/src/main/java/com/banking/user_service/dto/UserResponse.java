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
public class UserResponse {
    private String internalUserId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private UserStatus status;
    private OnboardingStatus onboardingStatus;
}