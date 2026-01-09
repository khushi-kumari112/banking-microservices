package com.banking.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from User Service
 * Contains user verification details needed for account creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerificationResponse {

    /**
     * Internal user ID from User Service
     */
    private String internalUserId;

    /**
     * User's full name
     */
    private String fullName;

    /**
     * User's email (for notifications)
     */
    private String email;

    /**
     * User's phone number (for SMS alerts)
     */
    private String phone;

    /**
     * KYC status: PENDING, VERIFIED, REJECTED
     * Account creation allowed only if VERIFIED
     */
    private String kycStatus;

    /**
     * User's onboarding status: COMPLETED, INCOMPLETE
     */
    private String onboardingStatus;

    /**
     * Is user active?
     */
    private boolean Active;

    private boolean isBlocked;

    /**
     * Date of birth (for age verification)
     */
    private String dateOfBirth;

    /**
     * Helper method: Check if KYC is verified
     */
    public boolean isKycVerified() {
        return "VERIFIED".equalsIgnoreCase(kycStatus);
    }

    /**
     * Helper method: Check if onboarding is complete
     */
    public boolean isOnboardingComplete() {
        return "COMPLETED".equalsIgnoreCase(onboardingStatus);
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
            * Helper method: Check if user can create account
     */


    public boolean canCreateAccount() {
        return Active && isKycVerified();
    }
}
