package com.banking.user_service.model;

import com.banking.user_service.enums.OtpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTP Data Model for Redis Storage
 * Stores temporary OTP information in Redis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpData implements Serializable {

    private String otpCode;
    private OtpType otpType;
    private String identifier; // email or phone
    private LocalDateTime expiryDate;
    private boolean verified;
    private int attemptCount;
    private LocalDateTime createdDate;
}