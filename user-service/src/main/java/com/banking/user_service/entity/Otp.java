package com.banking.user_service.entity;

import com.banking.user_service.enums.OtpType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * OTP Entity - Stores OTPs for Email and Phone Verification
 *
 * Real-world banking approach:
 * - Supports both email and phone OTP verification
 * - OTPs expire after 5 minutes
 * - Each OTP can be used only once (verified flag)
 * - Indexed for fast lookup by email and phone
 *
 * @author Khushi Kumari
 * @version 2.0
 */
@Entity
@Table(name = "otp", indexes = {
        @Index(name = "idx_email_otp", columnList = "email"),
        @Index(name = "idx_phone_otp", columnList = "phone")  // ← NEW INDEX for phone
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address for email OTP verification
     * Used for EMAIL_VERIFICATION and MPIN_RESET OTP types
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Phone number for phone OTP verification
     * Used for PHONE_VERIFICATION OTP type
     * Real-world: SMS OTP sent to this number
     */
    @Column(name = "phone", length = 15)
    private String phone;

    /**
     * 6-digit OTP code
     * Generated randomly and sent to user
     */
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    /**
     * Type of OTP: EMAIL_VERIFICATION, PHONE_VERIFICATION, or MPIN_RESET
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 30)
    private OtpType otpType;

    /**
     * OTP expiry time (5 minutes from creation)
     * Real-world: Banking OTPs expire quickly for security
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Whether OTP has been used/verified
     * Real-world: OTP can only be used once
     */
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    /**
     * Timestamp when OTP was created
     */
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
}
