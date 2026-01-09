package com.banking.user_service.service;

import com.banking.user_service.dto.AdminRegistrationRequest;
import com.banking.user_service.dto.AdminRegistrationResponse;
import com.banking.user_service.entity.Mpin;
import com.banking.user_service.entity.User;
import com.banking.user_service.enums.*;
import com.banking.user_service.exception.custom.InvalidInputException;
import com.banking.user_service.exception.custom.UserAlreadyExistsException;
import com.banking.user_service.repository.MpinRepository;
import com.banking.user_service.repository.UserRepository;
import com.banking.user_service.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Admin Service Implementation
 * Handles admin registration and management
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final MpinRepository mpinRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;

    @Value("${admin.registration.secret-key:BANKING_ADMIN_SECRET_2025}")
    private String adminSecretKey;

    @Override
    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request, String providedSecretKey) {
        log.info(" Processing admin registration for: {}", request.getEmail());

        // Validate secret key
        if (!adminSecretKey.equals(providedSecretKey)) {
            log.error(" Invalid admin secret key provided");
            throw new InvalidInputException("Secret Key", "Invalid admin secret key");
        }

        // Sanitize inputs
        String firstName = inputSanitizer.sanitizeInput(request.getFirstName());
        String middleName = inputSanitizer.sanitizeInput(request.getMiddleName());
        String lastName = inputSanitizer.sanitizeInput(request.getLastName());
        String email = inputSanitizer.sanitizeEmailInput(request.getEmail());
        String phone = inputSanitizer.sanitizePhoneInput(request.getPhone());

        // Check if email or phone already exists
        if (userRepository.existsByEmail(email)) {
            log.error(" Admin email already exists: {}", email);
            throw new UserAlreadyExistsException("email", email);
        }

        if (userRepository.existsByPhone(phone)) {
            log.error(" Admin phone already exists: {}", phone);
            throw new UserAlreadyExistsException("phone", phone);
        }

        // Validate MPIN
        validateMpin(request.getMpin(), request.getConfirmMpin());

        // Generate internal user ID
        String internalUserId = generateInternalUserId("ADM");

        // Create admin user
        User admin = new User();
        admin.setInternalUserId(internalUserId);
        admin.setFirstName(firstName);
        admin.setMiddleName(middleName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPhone(phone);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(UserRole.ADMIN);
        admin.setOnboardingStatus(OnboardingStatus.MPIN_CREATED);
        admin.setKycStatus(KycStatus.VERIFIED); // Admins are pre-verified
        admin.setCreatedDate(LocalDateTime.now());

        User savedAdmin = userRepository.save(admin);
        log.info(" Admin user created: {}", internalUserId);

        // Create MPIN
        Mpin mpin = new Mpin();
        mpin.setInternalUserId(internalUserId);
        mpin.setHashedMpin(passwordEncoder.encode(request.getMpin()));
        mpin.setMpinStatus(MpinStatus.ACTIVE);
        mpin.setWrongAttempts(0);
        mpin.setCreatedDate(LocalDateTime.now());
        mpinRepository.save(mpin);
        log.info(" Admin MPIN created: {}", internalUserId);

        // Build response
        return AdminRegistrationResponse.builder()
                .internalUserId(savedAdmin.getInternalUserId())
                .firstName(savedAdmin.getFirstName())
                .middleName(savedAdmin.getMiddleName())
                .lastName(savedAdmin.getLastName())
                .fullName(savedAdmin.getFullName())
                .email(savedAdmin.getEmail())
                .phone(savedAdmin.getPhone())
                .role(savedAdmin.getRole().toString())
                .status(savedAdmin.getStatus().toString())
                .message("Admin registered successfully. You can now login with email/phone and MPIN.")
                .build();
    }

    private void validateMpin(String mpin, String confirmMpin) {
        if (!mpin.equals(confirmMpin)) {
            throw new InvalidInputException("MPIN", "MPIN and Confirm MPIN do not match");
        }

        if (mpin.length() < 4 || mpin.length() > 6) {
            throw new InvalidInputException("MPIN", "MPIN must be 4 to 6 digits");
        }

        if (!mpin.matches("\\d+")) {
            throw new InvalidInputException("MPIN", "MPIN must contain only digits");
        }

        // Check for weak MPINs
        String[] weakMpins = {"0000", "1111", "2222", "3333", "4444",
                "5555", "6666", "7777", "8888", "9999",
                "1234", "4321", "000000", "111111", "123456"};

        for (String weak : weakMpins) {
            if (mpin.equals(weak)) {
                throw new InvalidInputException("MPIN",
                        "This MPIN is too common. Please choose a stronger MPIN.");
            }
        }
    }

    private String generateInternalUserId(String prefix) {
        long timestamp = System.currentTimeMillis() % 1000000;
        int random = new Random().nextInt(9000) + 1000;
        return String.format("%s_%d%d", prefix, timestamp, random);
    }
}