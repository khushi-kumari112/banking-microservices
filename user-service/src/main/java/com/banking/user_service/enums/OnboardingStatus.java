
package com.banking.user_service.enums;

public enum OnboardingStatus {
    EMAIL_PENDING,      // Email not verified
    EMAIL_VERIFIED,     // Email verified via OTP
    MPIN_CREATED,       // MPIN set successfully
    MPIN_LOCKED,        // MPIN locked due to wrong attempts
    MPIN_TEMPORARILY_LOCKED, // Temporary lock (couple of minutes)
    PHONE_PENDING_VERIFICATION,
    PHONE_VERIFIED,
    ONBOARDING_COMPLETED // Full onboarding done
}