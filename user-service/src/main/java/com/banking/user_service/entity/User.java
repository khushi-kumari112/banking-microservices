package com.banking.user_service.entity;

import com.banking.user_service.enums.KycStatus;
import com.banking.user_service.enums.OnboardingStatus;
import com.banking.user_service.enums.UserRole;
import com.banking.user_service.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_phone", columnList = "phone", unique = true),
        @Index(name = "idx_internal_user_id", columnList = "internal_user_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "internal_user_id", unique = true, nullable = false, length = 50)
    private String internalUserId; // Format: USR_XXXXXX

    // Separate Name Fields
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", unique = true, length = 100 ,nullable = true)
    private String email;

    @Column(name = "phone", unique = true, length = 15 ,nullable = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false, length = 30)
    private OnboardingStatus onboardingStatus = OnboardingStatus.EMAIL_PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "kyc_document_path", length = 500)
    private String kycDocumentPath;

    @Column(name = "kyc_submitted_date")
    private LocalDateTime kycSubmittedDate;

    @Column(name = "kyc_verified_date")
    private LocalDateTime kycVerifiedDate;

    @Column(name = "kyc_rejection_reason", length = 500)
    private String kycRejectionReason;

    // Helper method to get full name
    public String getFullName() {
        return middleName != null && !middleName.isEmpty()
                ? firstName + " " + middleName + " " + lastName
                : firstName + " " + lastName;
    }
}