package com.banking.user_service.enums;

public enum KycStatus {
    PENDING,// User signed up, KYC not submitted
    VERIFIED, // Admin approved KYC
    REJECTED // Admin rejected KYC (invalid documents)
}
