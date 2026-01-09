package com.banking.user_service.service;

import com.banking.user_service.dto.*;

/**
 * User Service Interface
 * Defines all user-related operations for banking microservice
 *
 */
public interface UserService {

    //  REGISTRATION
    UserResponse registerName(NameRegistrationRequest request, String ipAddress);
    void registerEmail(String internalUserId, EmailRegistrationRequest request);
    UserResponse verifyOtp(String internalUserId, String email, OtpVerificationRequest request);

    // Phone Registration with OTP (NEW)
    void registerPhone(String internalUserId, PhoneRegistrationRequest request);  // ← CHANGED TO void
    UserResponse verifyPhoneOtp(String internalUserId, String phone, OtpVerificationRequest request);  // ← NEW METHOD

    UserResponse createMpin(String internalUserId, MpinCreationRequest request);

    // AUTHENTICATION
    LoginResponse login(String identifier, LoginRequest request, String ipAddress);
    void requestMpinResetOtp(String identifier);
    UserResponse resetMpin(String identifier, String otpCode, MpinResetRequest request);

    // PROFILE MANAGEMENT
    UserResponse getUserByInternalId(String internalUserId);
    UserResponse updateName(String internalUserId, NameRegistrationRequest request);

    // ADDRESS MANAGEMENT
    AddressResponse addOrUpdateAddress(String internalUserId, AddressRequest request);
    AddressResponse getAddress(String internalUserId);

    // KYC MANAGEMENT
    UserResponse submitKYC(String internalUserId, KYCSubmissionRequest request);
    KycStatusResponse getKYCStatus(String internalUserId);
    UserResponse approveKYC(String internalUserId);
    UserResponse rejectKYC(String internalUserId, String reason);

    // ADMIN OPERATIONS
    void unblockUser(String internalUserId);
}
