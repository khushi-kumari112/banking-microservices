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
public class KycStatusResponse {
    private String internalUserId;
    private KycStatus kycStatus;
    private String documentType;
    private String submittedDate;
    private String verifiedDate;
    private String rejectionReason;
}