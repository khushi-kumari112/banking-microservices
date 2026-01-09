package com.banking.user_service.service;

import com.banking.user_service.enums.OtpType;
import com.banking.user_service.exception.custom.InvalidInputException;
import com.banking.user_service.exception.custom.OtpExpiredException;
import com.banking.user_service.model.OtpData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based OTP Service
 * Handles OTP storage and validation using Redis for temporary storage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisOtpService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final int MAX_OTP_ATTEMPTS = 3;

    /**
     * Generate and store OTP in Redis
     */
    public String generateAndStoreOtp(String identifier, OtpType otpType) {
        String otpCode = generateOtp();
        String redisKey = buildRedisKey(identifier, otpType);

        OtpData otpData = OtpData.builder()
                .otpCode(otpCode)
                .otpType(otpType)
                .identifier(identifier)
                .expiryDate(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verified(false)
                .attemptCount(0)
                .createdDate(LocalDateTime.now())
                .build();

        // Store in Redis with expiry
        redisTemplate.opsForValue().set(redisKey, otpData, otpExpiryMinutes, TimeUnit.MINUTES);

        log.info(" OTP generated and stored in Redis: {} for {}", redisKey, identifier);
        return otpCode;
    }

    /**
     * Validate OTP from Redis
     */
    public void validateOtp(String identifier, OtpType otpType, String providedOtp) {
        String redisKey = buildRedisKey(identifier, otpType);

        OtpData otpData = (OtpData) redisTemplate.opsForValue().get(redisKey);

        if (otpData == null) {
            log.error("OTP not found or expired in Redis: {}", redisKey);
            throw new InvalidInputException("OTP", "Invalid or expired OTP");
        }

        // Check if already verified
        if (otpData.isVerified()) {
            log.error(" OTP already used: {}", redisKey);
            throw new InvalidInputException("OTP", "This OTP has already been used");
        }

        // Check expiry
        if (LocalDateTime.now().isAfter(otpData.getExpiryDate())) {
            redisTemplate.delete(redisKey);
            log.error(" OTP expired: {}", redisKey);
            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        // Check attempt count
        if (otpData.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            redisTemplate.delete(redisKey);
            log.error(" Max OTP attempts exceeded: {}", redisKey);
            throw new InvalidInputException("OTP", "Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        // Validate OTP code
        if (!otpData.getOtpCode().equals(providedOtp)) {
            otpData.setAttemptCount(otpData.getAttemptCount() + 1);
            redisTemplate.opsForValue().set(redisKey, otpData, otpExpiryMinutes, TimeUnit.MINUTES);

            int remainingAttempts = MAX_OTP_ATTEMPTS - otpData.getAttemptCount();
            log.error(" Invalid OTP code. Remaining attempts: {}", remainingAttempts);
            throw new InvalidInputException("OTP",
                    String.format("Invalid OTP code. %d attempts remaining.", remainingAttempts));
        }

        // Mark as verified
        otpData.setVerified(true);
        redisTemplate.opsForValue().set(redisKey, otpData, 5, TimeUnit.MINUTES); // Keep for 5 more minutes

        log.info(" OTP verified successfully: {}", redisKey);
    }

    /**
     * Delete OTP from Redis
     */
    public void deleteOtp(String identifier, OtpType otpType) {
        String redisKey = buildRedisKey(identifier, otpType);
        redisTemplate.delete(redisKey);
        log.info("🗑 OTP deleted from Redis: {}", redisKey);
    }

    /**
     * Check if OTP exists and is valid
     */
    public boolean isOtpValid(String identifier, OtpType otpType) {
        String redisKey = buildRedisKey(identifier, otpType);
        OtpData otpData = (OtpData) redisTemplate.opsForValue().get(redisKey);

        if (otpData == null) {
            return false;
        }

        return !otpData.isVerified() && LocalDateTime.now().isBefore(otpData.getExpiryDate());
    }

    /**
     * Generate random OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int otp = min + random.nextInt(max - min + 1);
        return String.valueOf(otp);
    }

    /**
     * Build Redis key for OTP storage
     * Pattern: otp:{type}:{identifier}
     */
    private String buildRedisKey(String identifier, OtpType otpType) {
        return String.format("%s%s:%s", OTP_KEY_PREFIX, otpType.name().toLowerCase(), identifier);
    }
}