
package com.banking.user_service.repository;

import com.banking.user_service.entity.Otp;
import com.banking.user_service.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    // Existing methods
    Optional<Otp> findByEmailAndOtpTypeAndVerifiedFalseAndExpiryDateAfter(
            String email, OtpType otpType, LocalDateTime currentTime);
    void deleteByEmailAndOtpType(String email, OtpType otpType);

    // NEW: Add these for phone OTP
    Optional<Otp> findByPhoneAndOtpTypeAndVerifiedFalseAndExpiryDateAfter(
            String phone, OtpType otpType, LocalDateTime currentTime);
    void deleteByPhoneAndOtpType(String phone, OtpType otpType);
}
