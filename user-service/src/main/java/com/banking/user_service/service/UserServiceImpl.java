package com.banking.user_service.service;

import com.banking.user_service.config.AppProperties;
import com.banking.user_service.dto.*;
import com.banking.user_service.entity.*;
import com.banking.user_service.enums.*;
import com.banking.user_service.event.*;
import com.banking.user_service.exception.custom.*;
import com.banking.user_service.kafka.KafkaEventProducer;
import com.banking.user_service.repository.*;
import com.banking.user_service.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MpinRepository mpinRepository;
    private final UserAddressRepository addressRepository;
    private final OtpRepository otpRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserActivityRepository userActivityRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer inputSanitizer;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;

    private static final int MAX_WRONG_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final int OTP_EXPIRY_MINUTES = 5;

    //  REGISTRATION

    @Override
    @Transactional
    public UserResponse registerName(NameRegistrationRequest request, String ipAddress) {
        log.info(" Starting name registration from IP: {}", ipAddress);

        String firstName = inputSanitizer.sanitizeInput(request.getFirstName());
        String middleName = inputSanitizer.sanitizeInput(request.getMiddleName());
        String lastName = inputSanitizer.sanitizeInput(request.getLastName());

        String internalUserId = generateInternalUserId();

        User user = new User();
        user.setInternalUserId(internalUserId);
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.CUSTOMER);
        user.setOnboardingStatus(OnboardingStatus.EMAIL_PENDING);
        user.setKycStatus(KycStatus.PENDING);
        user.setCreatedDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        createAuditLog(internalUserId, AuditAction.USER_REGISTRATION, "Name registration completed", ipAddress, null);

        log.info(" Name registered successfully - Internal User ID: {}", internalUserId);
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public void registerEmail(String internalUserId, EmailRegistrationRequest request) {
        log.info(" Processing email registration for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validateOnboardingStatusForOperation(user, OnboardingStatus.EMAIL_PENDING, "Email registration");

        String email = inputSanitizer.sanitizeEmailInput(request.getEmail());
        validateEmailDomain(email);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }

        String otpCode = generateOTP();
        createAndSaveOtp(email, otpCode, OtpType.EMAIL_VERIFICATION);

        user.setEmail(email);
        userRepository.save(user);

        emailService.sendOtpEmail(email, otpCode, user.getFullName());
        log.info(" OTP sent to email: {} for user: {}", email, internalUserId);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserResponse verifyOtp(String internalUserId, String email, OtpVerificationRequest request) {
        log.info(" Verifying email OTP for user: {}, email: {}", internalUserId, email);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validateEmailMatches(user, email);

        Otp otp = getValidOtp(email, OtpType.EMAIL_VERIFICATION);
        validateOtpCode(otp, request.getOtp());

        otp.setVerified(true);
        otpRepository.save(otp);

        user.setOnboardingStatus(OnboardingStatus.EMAIL_VERIFIED);
        user.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        createAuditLog(internalUserId, AuditAction.EMAIL_VERIFIED, "Email verified successfully", null, null);
        log.info(" Email verified successfully for user: {}", internalUserId);

        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void registerPhone(String internalUserId, PhoneRegistrationRequest request) {
        log.info(" Processing phone registration for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validateOnboardingStatusForOperation(user, OnboardingStatus.EMAIL_VERIFIED, "Phone registration");

        String phone = inputSanitizer.sanitizePhoneInput(request.getPhone());

        if (userRepository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("phone", phone);
        }

        // Generate and send SMS OTP (REAL-WORLD APPROACH)
        String otpCode = generateOTP();
        createAndSaveOtpForPhone(phone, otpCode, OtpType.PHONE_VERIFICATION);

        user.setPhone(phone);
        user.setOnboardingStatus(OnboardingStatus.PHONE_PENDING_VERIFICATION);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);

        // Send SMS OTP (logs to console for now, will integrate with SMS gateway later)
        emailService.sendPhoneOtp(phone, otpCode, user.getFullName());

        createAuditLog(internalUserId, AuditAction.PHONE_REGISTERED,
                "Phone registered, OTP sent", null, null);

        log.info(" Phone registered, SMS OTP sent to: {}", phone);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserResponse verifyPhoneOtp(String internalUserId, String phone, OtpVerificationRequest request) {
        log.info(" Verifying phone OTP for user: {}, phone: {}", internalUserId, phone);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validatePhoneMatches(user, phone);

        Otp otp = getValidOtpForPhone(phone, OtpType.PHONE_VERIFICATION);
        validateOtpCode(otp, request.getOtp());

        otp.setVerified(true);
        otpRepository.save(otp);

        user.setOnboardingStatus(OnboardingStatus.PHONE_VERIFIED);
        user.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        createAuditLog(internalUserId, AuditAction.PHONE_VERIFIED,
                "Phone verified successfully", null, null);

        log.info(" Phone verified successfully for user: {}", internalUserId);
        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserResponse createMpin(String internalUserId, MpinCreationRequest request) {
        log.info(" Creating MPIN for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);

        // Updated validation: Must have phone verified before creating MPIN
        validateOnboardingStatusForOperation(user, OnboardingStatus.PHONE_VERIFIED, "MPIN creation");

        if (user.getOnboardingStatus() == OnboardingStatus.MPIN_CREATED) {
            throw new InvalidInputException("MPIN", "MPIN already created");
        }

        validateMpinMatchForCreation(request.getMpin(), request.getConfirmMpin());
        validateMpinStrength(request.getMpin());

        if (mpinRepository.existsByInternalUserId(internalUserId)) {
            throw new InvalidInputException("MPIN", "MPIN already exists for this user");
        }

        Mpin mpin = new Mpin();
        mpin.setInternalUserId(internalUserId);
        mpin.setHashedMpin(passwordEncoder.encode(request.getMpin()));
        mpin.setMpinStatus(MpinStatus.ACTIVE);
        mpin.setWrongAttempts(0);
        mpin.setCreatedDate(LocalDateTime.now());
        mpinRepository.save(mpin);

        user.setOnboardingStatus(OnboardingStatus.MPIN_CREATED);
        user.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        createAuditLog(internalUserId, AuditAction.MPIN_CREATED, "MPIN created successfully", null, null);
        publishUserRegisteredEvent(updatedUser);

        log.info(" MPIN created successfully for user: {}", internalUserId);
        return convertToResponse(updatedUser);
    }

    //  AUTHENTICATION

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LoginResponse login(String identifier, LoginRequest request, String ipAddress) {
        log.info(" Processing login for identifier: {}", identifier);

        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> {
                    log.error(" User not found with identifier: {}", identifier);
                    return new UserNotFoundException("identifier", identifier);
                });

        String internalUserId = user.getInternalUserId();
        validateUserStatus(user);

        Mpin mpin = mpinRepository.findByInternalUserId(internalUserId)
                .orElseThrow(() -> new InvalidInputException("MPIN", "MPIN not set for this user"));

        checkMpinLockStatus(mpin);
        validateMpinMatch(mpin, request.getMpin(), user);
        handleSuccessfulLogin(mpin, user, ipAddress);

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole().name());

        log.info(" Login successful for user: {}", internalUserId);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .internalUserId(internalUserId)
                .fullName(user.getFullName())
                .expiresIn(86400000L)
                .build();
    }

    @Override
    @Transactional
    public void requestMpinResetOtp(String identifier) {
        log.info(" MPIN reset OTP requested for: {}", identifier);

        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new UserNotFoundException("identifier", identifier));

        String otpCode = generateOTP();
        createAndSaveOtp(user.getEmail(), otpCode, OtpType.MPIN_RESET);

        emailService.sendMpinResetOtp(user.getEmail(), otpCode, user.getFullName());
        log.info(" MPIN reset OTP sent to: {}", user.getEmail());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserResponse resetMpin(String identifier, String otpCode, MpinResetRequest request) {
        log.info(" Resetting MPIN for: {}", identifier);

        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new UserNotFoundException("identifier", identifier));

        String email = user.getEmail();
        String internalUserId = user.getInternalUserId();

        Otp otp = getValidOtp(email, OtpType.MPIN_RESET);
        validateOtpCode(otp, otpCode);

        validateMpinMatchForCreation(request.getNewMpin(), request.getConfirmNewMpin());
        validateMpinStrength(request.getNewMpin());

        Mpin mpin = mpinRepository.findByInternalUserId(internalUserId)
                .orElseThrow(() -> new InvalidInputException("MPIN", "MPIN not found"));

        validateNewMpinDifferent(mpin, request.getNewMpin());

        updateMpinAndUnlock(mpin, request.getNewMpin());
        otp.setVerified(true);
        otpRepository.save(otp);

        unlockUserIfNeeded(user);

        createAuditLog(internalUserId, AuditAction.MPIN_RESET, "MPIN reset successfully", null, null);
        log.info(" MPIN reset successfully for user: {}", internalUserId);

        return convertToResponse(user);
    }

    //  PROFILE MANAGEMENT

    @Override
    public UserResponse getUserByInternalId(String internalUserId) {
        log.info(" Fetching user profile: {}", internalUserId);
        User user = getUserByInternalIdOrThrow(internalUserId);
        return convertToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateName(String internalUserId, NameRegistrationRequest request) {
        log.info(" Updating name for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);

        user.setFirstName(inputSanitizer.sanitizeInput(request.getFirstName()));
        user.setMiddleName(inputSanitizer.sanitizeInput(request.getMiddleName()));
        user.setLastName(inputSanitizer.sanitizeInput(request.getLastName()));
        user.setUpdatedDate(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        createAuditLog(internalUserId, AuditAction.PROFILE_UPDATE, "Name updated", null, null);

        return convertToResponse(updatedUser);
    }

    //  ADDRESS MANAGEMENT

    @Override
    @Transactional
    public AddressResponse addOrUpdateAddress(String internalUserId, AddressRequest request) {
        log.info(" Saving address for user: {}", internalUserId);

        getUserByInternalIdOrThrow(internalUserId);

        UserAddress address = addressRepository.findByInternalUserId(internalUserId)
                .orElse(new UserAddress());

        address.setInternalUserId(internalUserId);
        address.setAddressLine1(inputSanitizer.sanitizeInput(request.getAddressLine1()));
        address.setAddressLine2(inputSanitizer.sanitizeInput(request.getAddressLine2()));
        address.setCity(inputSanitizer.sanitizeInput(request.getCity()));
        address.setState(inputSanitizer.sanitizeInput(request.getState()));
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());

        if (address.getId() == null) {
            address.setCreatedDate(LocalDateTime.now());
        } else {
            address.setUpdatedDate(LocalDateTime.now());
        }

        UserAddress savedAddress = addressRepository.save(address);
        createAuditLog(internalUserId, AuditAction.ADDRESS_UPDATED, "Address saved", null, null);

        return convertToAddressResponse(savedAddress);
    }

    @Override
    public AddressResponse getAddress(String internalUserId) {
        log.info(" Fetching address for user: {}", internalUserId);

        UserAddress address = addressRepository.findByInternalUserId(internalUserId)
                .orElseThrow(() -> new InvalidInputException("Address", "Address not found for this user"));

        return convertToAddressResponse(address);
    }

    //  KYC MANAGEMENT

    @Override
    @Transactional
    public UserResponse submitKYC(String internalUserId, KYCSubmissionRequest request) {
        log.info(" Processing KYC submission for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);

        if (user.getKycStatus() == KycStatus.VERIFIED) {
            throw new InvalidInputException("KYC", "KYC already verified");
        }

        String documentInfo = String.format("%s: %s - %s",
                request.getDocumentType(), request.getDocumentNumber(), request.getDocumentPath());

        user.setKycDocumentPath(documentInfo);
        user.setKycStatus(KycStatus.PENDING);
        user.setKycSubmittedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        createAuditLog(internalUserId, AuditAction.KYC_SUBMITTED, "KYC documents submitted", null, null);

        return convertToResponse(updatedUser);
    }

    @Override
    public KycStatusResponse getKYCStatus(String internalUserId) {
        log.info(" Fetching KYC status for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return KycStatusResponse.builder()
                .internalUserId(internalUserId)
                .kycStatus(user.getKycStatus())
                .documentType(extractDocumentType(user.getKycDocumentPath()))
                .submittedDate(user.getKycSubmittedDate() != null ? user.getKycSubmittedDate().format(formatter) : null)
                .verifiedDate(user.getKycVerifiedDate() != null ? user.getKycVerifiedDate().format(formatter) : null)
                .rejectionReason(user.getKycRejectionReason())
                .build();
    }

    @Override
    @Transactional
    public UserResponse approveKYC(String internalUserId) {
        log.info(" Approving KYC for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validateKycStatusForOperation(user, KycStatus.PENDING, "approve");

        user.setKycStatus(KycStatus.VERIFIED);
        user.setKycVerifiedDate(LocalDateTime.now());
        user.setKycRejectionReason(null);
        user.setUpdatedDate(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        createAuditLog(internalUserId, AuditAction.KYC_APPROVED, "KYC verified by admin", null, "ADMIN");
        publishKycVerifiedEvent(updatedUser);

        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse rejectKYC(String internalUserId, String reason) {
        log.info(" Rejecting KYC for user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        validateKycStatusForOperation(user, KycStatus.PENDING, "reject");

        user.setKycStatus(KycStatus.REJECTED);
        user.setKycRejectionReason(reason);
        user.setUpdatedDate(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        createAuditLog(internalUserId, AuditAction.KYC_REJECTED, "KYC rejected - " + reason, null, "ADMIN");

        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void unblockUser(String internalUserId) {
        log.info(" Unblocking user: {}", internalUserId);

        User user = getUserByInternalIdOrThrow(internalUserId);
        Mpin mpin = mpinRepository.findByInternalUserId(internalUserId)
                .orElseThrow(() -> new InvalidInputException("MPIN", "MPIN not found"));

        resetMpinLock(mpin);
        unlockUserIfNeeded(user);

        createAuditLog(internalUserId, AuditAction.USER_UNBLOCKED, "User unblocked by admin", null, "ADMIN");
        log.info(" User unblocked successfully: {}", internalUserId);
    }

    //  PRIVATE VALIDATION METHODS

    private void validateOnboardingStatusForOperation(User user, OnboardingStatus expected, String operation) {
        if (user.getOnboardingStatus() != expected) {
            log.error("Invalid onboarding status for {}. Expected: {}, Found: {}",
                    operation, expected, user.getOnboardingStatus());
            throw new OnboardingIncompleteException(
                    user.getOnboardingStatus().toString(),
                    expected.toString()
            );
        }
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            log.error("User account is blocked: {}", user.getInternalUserId());
            throw new AccountBlockedException(
                    "Your account has been blocked. Please contact support."
            );
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.error("User account is suspended: {}", user.getInternalUserId());
            throw new AccountBlockedException(
                    "Your account has been suspended. Please contact support."
            );
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            log.error("User account is inactive: {}", user.getInternalUserId());
            throw new AccountBlockedException(
                    "Your account is inactive. Please reactivate your account."
            );
        }
    }

    private void checkMpinLockStatus(Mpin mpin) {
        if (mpin.getMpinStatus() == MpinStatus.LOCKED || mpin.getMpinStatus() == MpinStatus.TEMPORARILY_LOCKED) {
            LocalDateTime lockUntil = mpin.getLockUntil();

            if (lockUntil != null && LocalDateTime.now().isAfter(lockUntil)) {
                mpin.setMpinStatus(MpinStatus.ACTIVE);
                mpin.setWrongAttempts(0);
                mpin.setLockUntil(null);
                mpinRepository.save(mpin);
                log.info(" MPIN auto-unlocked after lock period for user: {}", mpin.getInternalUserId());
            } else if (lockUntil != null) {
                long minutesLeft = Duration.between(LocalDateTime.now(), lockUntil).toMinutes();
                log.error(" MPIN is locked for user: {}, minutes left: {}",
                        mpin.getInternalUserId(), minutesLeft);
                throw new AccountBlockedException(
                        String.format("MPIN temporarily locked. Try again after %d minutes", minutesLeft + 1)
                );
            } else {
                throw new AccountBlockedException("MPIN is permanently locked. Please contact support or reset MPIN");
            }
        }
    }

    private void validateOtpCode(Otp otp, String providedOtp) {
        if (otp.getVerified()) {
            log.error(" OTP already used: {}", otp.getOtpCode());
            throw new InvalidInputException("OTP", "This OTP has already been used");
        }

        if (LocalDateTime.now().isAfter(otp.getExpiryDate())) {
            log.error(" OTP expired: {}", otp.getOtpCode());
            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        if (!otp.getOtpCode().equals(providedOtp)) {
            log.error(" Invalid OTP code provided");
            throw new InvalidInputException("OTP", "Invalid OTP code");
        }
    }

    private void validateMpinMatch(Mpin mpin, String providedMpin, User user) {
        if (!passwordEncoder.matches(providedMpin, mpin.getHashedMpin())) {
            int wrongAttempts = mpin.getWrongAttempts() + 1;
            mpin.setWrongAttempts(wrongAttempts);
            mpin.setUpdatedDate(LocalDateTime.now());

            if (wrongAttempts >= MAX_WRONG_ATTEMPTS) {
                lockMpin(mpin, user, null);
            }

            mpinRepository.save(mpin);

            int remainingAttempts = MAX_WRONG_ATTEMPTS - wrongAttempts;
            log.error(" Invalid MPIN for user: {}, remaining attempts: {}",
                    user.getInternalUserId(), remainingAttempts);
            throw new InvalidMPINException(remainingAttempts);
        }

        if (mpin.getWrongAttempts() > 0) {
            mpin.setWrongAttempts(0);
            mpin.setUpdatedDate(LocalDateTime.now());
            mpinRepository.save(mpin);
        }
    }

    private void validateMpinMatchForCreation(String mpin, String confirmMpin) {
        if (!mpin.equals(confirmMpin)) {
            log.error(" MPIN and Confirm MPIN do not match");
            throw new MPINMismatchException();
        }
    }

    private void validateMpinStrength(String mpin) {
        if (mpin == null || mpin.trim().isEmpty()) {
            throw new InvalidInputException("MPIN", "MPIN cannot be empty");
        }

        String[] weakMpins = {"0000", "1111", "2222", "3333", "4444",
                "5555", "6666", "7777", "8888", "9999",
                "1234", "4321", "0123", "000000", "111111",
                "123456", "654321"};

        for (String weak : weakMpins) {
            if (mpin.equals(weak)) {
                throw new InvalidInputException("MPIN",
                        "This MPIN is too common. Please choose a stronger MPIN.");
            }
        }

        boolean isSequential = true;
        for (int i = 0; i < mpin.length() - 1; i++) {
            if (Integer.parseInt(String.valueOf(mpin.charAt(i + 1))) !=
                    Integer.parseInt(String.valueOf(mpin.charAt(i))) + 1) {
                isSequential = false;
                break;
            }
        }
        if (isSequential) {
            throw new InvalidInputException("MPIN", "MPIN cannot be sequential (e.g., 1234, 123456)");
        }

        char firstChar = mpin.charAt(0);
        boolean isRepeated = true;
        for (int i = 1; i < mpin.length(); i++) {
            if (mpin.charAt(i) != firstChar) {
                isRepeated = false;
                break;
            }
        }
        if (isRepeated) {
            throw new InvalidInputException("MPIN", "MPIN cannot have all repeated digits (e.g., 1111, 000000)");
        }
    }

    private void validateEmailDomain(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();

        String[] blockedDomains = {
                "tempmail.com", "throwaway.email", "guerrillamail.com",
                "10minutemail.com", "mailinator.com"
        };

        for (String blocked : blockedDomains) {
            if (domain.equals(blocked)) {
                log.error(" Blocked email domain used: {}", domain);
                throw new InvalidEmailDomainException(
                        "Temporary or disposable email addresses are not allowed"
                );
            }
        }

        String[] validDomains = {".com", ".org", ".in", ".edu", ".gov", ".net", ".co.in"};
        boolean validDomain = false;

        for (String validExt : validDomains) {
            if (email.toLowerCase().endsWith(validExt)) {
                validDomain = true;
                break;
            }
        }

        if (!validDomain) {
            throw new InvalidEmailDomainException(
                    "Email must be from valid domain (.com, .org, .in, .edu, .gov, .net, .co.in)"
            );
        }
    }

    private void validateEmailMatches(User user, String providedEmail) {
        if (user.getEmail() == null) {
            throw new EmailNotVerifiedException(
                    "User email is not registered. Please complete email registration."
            );
        }

        String sanitizedProvidedEmail = inputSanitizer.sanitizeEmailInput(providedEmail);
        String userEmail = user.getEmail().toLowerCase().trim();

        if (!userEmail.equals(sanitizedProvidedEmail)) {
            log.error(" Email mismatch. User email: {}, Provided: {}",
                    userEmail, sanitizedProvidedEmail);
            throw new InvalidInputException("Email",
                    "Provided email does not match registered email"
            );
        }
    }

    private void validatePhoneMatches(User user, String providedPhone) {
        if (user.getPhone() == null) {
            throw new InvalidInputException("Phone",
                    "User phone is not registered. Please complete phone registration."
            );
        }

        String sanitizedProvidedPhone = inputSanitizer.sanitizePhoneInput(providedPhone);
        String userPhone = user.getPhone().trim();

        if (!userPhone.equals(sanitizedProvidedPhone)) {
            log.error(" Phone mismatch. User phone: {}, Provided: {}",
                    userPhone, sanitizedProvidedPhone);
            throw new InvalidInputException("Phone",
                    "Provided phone does not match registered phone"
            );
        }
    }

    private void validateKycStatusForOperation(User user, KycStatus expected, String operation) {
        if (user.getKycStatus() != expected) {
            log.error(" Invalid KYC status for {}. Expected: {}, Found: {}",
                    operation, expected, user.getKycStatus());
            throw new InvalidInputException("KYC",
                    String.format("Cannot %s KYC. Expected: %s, Current: %s",
                            operation, expected, user.getKycStatus()));
        }
    }

    private void validateNewMpinDifferent(Mpin existingMpin, String newMpin) {
        if (passwordEncoder.matches(newMpin, existingMpin.getHashedMpin())) {
            log.error(" New MPIN is same as old MPIN for user: {}",
                    existingMpin.getInternalUserId());
            throw new InvalidInputException("MPIN",
                    "New MPIN cannot be the same as your current MPIN. Please choose a different MPIN."
            );
        }
    }

    //  PRIVATE HELPER METHOD

    private User getUserByInternalIdOrThrow(String internalUserId) {
        return userRepository.findByInternalUserId(internalUserId)
                .orElseThrow(() -> new UserNotFoundException("Internal User ID", internalUserId));
    }

    private Otp getValidOtp(String email, OtpType otpType) {
        return otpRepository.findByEmailAndOtpTypeAndVerifiedFalseAndExpiryDateAfter(
                        email, otpType, LocalDateTime.now())
                .orElseThrow(() -> new InvalidInputException("OTP", "Invalid or expired OTP"));
    }

    private Otp getValidOtpForPhone(String phone, OtpType otpType) {
        return otpRepository.findByPhoneAndOtpTypeAndVerifiedFalseAndExpiryDateAfter(
                        phone, otpType, LocalDateTime.now())
                .orElseThrow(() -> new InvalidInputException("OTP", "Invalid or expired OTP"));
    }

    private void createAndSaveOtp(String email, String otpCode, OtpType otpType) {
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otp.setVerified(false);
        otp.setCreatedDate(LocalDateTime.now());
        otpRepository.save(otp);
    }

    private void createAndSaveOtpForPhone(String phone, String otpCode, OtpType otpType) {
        otpRepository.deleteByPhoneAndOtpType(phone, otpType);

        Otp otp = new Otp();
        otp.setPhone(phone);
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otp.setVerified(false);
        otp.setCreatedDate(LocalDateTime.now());
        otpRepository.save(otp);
    }

    private void lockMpin(Mpin mpin, User user, String ipAddress) {
        mpin.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        mpin.setMpinStatus(MpinStatus.TEMPORARILY_LOCKED);
        user.setOnboardingStatus(OnboardingStatus.MPIN_TEMPORARILY_LOCKED);
        userRepository.save(user);

        log.warn("MPIN locked for user: {} due to {} wrong attempts",
                user.getInternalUserId(), MAX_WRONG_ATTEMPTS);

        createAuditLog(user.getInternalUserId(), AuditAction.MPIN_LOCKED,
                "MPIN temporarily locked due to wrong attempts", ipAddress, null);
        mpinRepository.save(mpin);

        throw new AccountBlockedException(
                String.format("MPIN locked for %d minutes due to %d wrong attempts",
                        LOCK_DURATION_MINUTES, MAX_WRONG_ATTEMPTS));
    }

    private void handleSuccessfulLogin(Mpin mpin, User user, String ipAddress) {
        if (mpin.getWrongAttempts() > 0 || mpin.getLockUntil() != null) {
            resetMpinLock(mpin);

            if (user.getOnboardingStatus() == OnboardingStatus.MPIN_TEMPORARILY_LOCKED) {
                user.setOnboardingStatus(OnboardingStatus.MPIN_CREATED);
            }
        }

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        createAuditLog(user.getInternalUserId(), AuditAction.LOGIN_SUCCESS,
                "User logged in successfully", ipAddress, null);
        logUserActivity(user.getInternalUserId(), ActivityType.LOGIN, ipAddress);
        publishUserLoginEvent(user, ipAddress);
    }

    private void resetMpinLock(Mpin mpin) {
        mpin.setWrongAttempts(0);
        mpin.setLockUntil(null);
        mpin.setMpinStatus(MpinStatus.ACTIVE);
        mpin.setUpdatedDate(LocalDateTime.now());
        mpinRepository.save(mpin);
    }

    private void updateMpinAndUnlock(Mpin mpin, String newMpin) {
        mpin.setHashedMpin(passwordEncoder.encode(newMpin));
        mpin.setWrongAttempts(0);
        mpin.setLockUntil(null);
        mpin.setMpinStatus(MpinStatus.ACTIVE);
        mpin.setUpdatedDate(LocalDateTime.now());
        mpinRepository.save(mpin);
    }

    private void unlockUserIfNeeded(User user) {
        if (user.getOnboardingStatus() == OnboardingStatus.MPIN_LOCKED ||
                user.getOnboardingStatus() == OnboardingStatus.MPIN_TEMPORARILY_LOCKED) {
            user.setOnboardingStatus(OnboardingStatus.MPIN_CREATED);
            user.setUpdatedDate(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    private String generateInternalUserId() {
        String prefix = "USR";
        long timestamp = System.currentTimeMillis() % 1000000;
        int random = new Random().nextInt(9000) + 1000;
        return String.format("%s_%d%d", prefix, timestamp, random);
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String extractDocumentType(String documentPath) {
        if (documentPath == null) return null;
        return documentPath.split(":")[0];
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .internalUserId(user.getInternalUserId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .onboardingStatus(user.getOnboardingStatus())
                .build();
    }

    private AddressResponse convertToAddressResponse(UserAddress address) {
        return AddressResponse.builder()
                .internalUserId(address.getInternalUserId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .build();
    }

    private void createAuditLog(String internalUserId, AuditAction action,
                                String description, String ipAddress, String performedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setInternalUserId(internalUserId);
            auditLog.setAction(action);
            auditLog.setDescription(description);
            auditLog.setIpAddress(ipAddress);
            auditLog.setPerformedBy(performedBy);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error(" Failed to create audit log: {}", e.getMessage());
        }
    }

    private void logUserActivity(String internalUserId, ActivityType activityType, String ipAddress) {
        try {
            UserActivity activity = new UserActivity();
            activity.setInternalUserId(internalUserId);
            activity.setActivityType(activityType);
            activity.setIpAddress(ipAddress);
            activity.setTimestamp(LocalDateTime.now());
            userActivityRepository.save(activity);
        } catch (Exception e) {
            log.error(" Failed to log user activity: {}", e.getMessage());
        }
    }

    private void publishUserRegisteredEvent(User user) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(user.getUserId())
                    .name(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .eventType("USER_REGISTERED")
                    .timestamp(LocalDateTime.now())
                    .message("User registration completed successfully")
                    .build();
            kafkaEventProducer.publishUserRegisteredEvent(event);
        } catch (Exception e) {
            log.error(" Failed to publish user registered event: {}", e.getMessage());
        }
    }

    private void publishUserLoginEvent(User user, String ipAddress) {
        try {
            UserLoginEvent event = UserLoginEvent.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .ipAddress(ipAddress)
                    .loginTime(LocalDateTime.now())
                    .eventType("USER_LOGIN")
                    .build();
            kafkaEventProducer.publishUserLoginEvent(event);
        } catch (Exception e) {
            log.error(" Failed to publish user login event: {}", e.getMessage());
        }
    }

    private void publishKycVerifiedEvent(User user) {
        try {
            UserStatusChangedEvent event = UserStatusChangedEvent.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .oldStatus(user.getStatus())
                    .newStatus(user.getStatus())
                    .reason("KYC Verified - Full banking services available")
                    .timestamp(LocalDateTime.now())
                    .eventType("KYC_VERIFIED")
                    .build();
            kafkaEventProducer.publishUserStatusChangedEvent(event);
        } catch (Exception e) {
            log.error(" Failed to publish KYC verified event: {}", e.getMessage());
        }
    }
}
