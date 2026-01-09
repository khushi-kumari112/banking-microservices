package com.banking.user_service.controller;

import com.banking.user_service.dto.*;
import com.banking.user_service.service.UserService;
import com.banking.user_service.util.ApiResponse;
import com.banking.user_service.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // REGISTRATION

    /**
     * Step 1: Register Name
     * POST /api/v1/users/register/name
     */
    @PostMapping("/register/name")
    public ResponseEntity<ApiResponse<UserResponse>> registerName(
            @Valid @RequestBody NameRegistrationRequest request,
            HttpServletRequest httpRequest) {
        log.info(" Step 1: Name registration initiated");
        UserResponse response = userService.registerName(request, getClientIp(httpRequest));
        return ResponseUtil.success("Name registered successfully. Please verify your email.", response);
    }

    /**
     * Step 2: Register Email & Send OTP
     * POST /api/v1/users/register/email
     * Header: internal-user-id
     */
    @PostMapping("/register/email")
    public ResponseEntity<ApiResponse<String>> registerEmail(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody EmailRegistrationRequest request) {
        log.info(" Step 2: Email registration for user: {}", internalUserId);
        userService.registerEmail(internalUserId, request);
        return ResponseUtil.success("OTP sent to email successfully",
                "Please check your email and verify OTP within 5 minutes");
    }

    /**
     * Step 3: Verify Email OTP
     * POST /api/v1/users/register/verify-otp
     * Header: internal-user-id, email
     */
    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<UserResponse>> verifyOtp(
            @RequestHeader("internal-user-id") String internalUserId,
            @RequestHeader("email") String email,
            @Valid @RequestBody OtpVerificationRequest request) {
        log.info(" Step 3: Email OTP verification for user: {}, email: {}", internalUserId, email);
        UserResponse response = userService.verifyOtp(internalUserId, email, request);
        return ResponseUtil.success("Email verified successfully. Please register your phone number.", response);
    }

    /**
     * Step 4: Register Phone & Send SMS OTP
     * POST /api/v1/users/register/phone
     * Header: internal-user-id
     */
    @PostMapping("/register/phone")
    public ResponseEntity<ApiResponse<String>> registerPhone(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody PhoneRegistrationRequest request) {
        log.info(" Step 4: Phone registration for user: {}", internalUserId);
        userService.registerPhone(internalUserId, request);
        return ResponseUtil.success("SMS OTP sent to your phone",
                "Please verify your phone number within 5 minutes");
    }

    /**
     * Step 5: Verify Phone OTP (NEW)
     * POST /api/v1/users/register/verify-phone
     * Header: internal-user-id, phone
     */
    @PostMapping("/register/verify-phone")
    public ResponseEntity<ApiResponse<UserResponse>> verifyPhoneOtp(
            @RequestHeader("internal-user-id") String internalUserId,
            @RequestHeader("phone") String phone,
            @Valid @RequestBody OtpVerificationRequest request) {
        log.info(" Step 5: Phone OTP verification for user: {}, phone: {}", internalUserId, phone);
        UserResponse response = userService.verifyPhoneOtp(internalUserId, phone, request);
        return ResponseUtil.success("Phone verified successfully. Please create your MPIN.", response);
    }

    /**
     * Step 6: Create MPIN (Final Step)
     * POST /api/v1/users/register/create-mpin
     * Header: internal-user-id
     */
    @PostMapping("/register/create-mpin")
    public ResponseEntity<ApiResponse<UserResponse>> createMpin(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody MpinCreationRequest request) {
        log.info(" Step 6: MPIN creation for user: {}", internalUserId);
        UserResponse response = userService.createMpin(internalUserId, request);
        return ResponseUtil.success("MPIN created successfully. Registration completed!", response);
    }

    // AUTHENTICATION

    /**
     * User Login with MPIN
     * POST /api/v1/users/authenticate
     * Header: identifier (email or phone)
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticate(
            @RequestHeader("identifier") String identifier,
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info(" Authentication request for identifier: {}", identifier);
        LoginResponse response = userService.login(identifier, request, getClientIp(httpRequest));
        return ResponseUtil.success("Authentication successful", response);
    }

    /**
     * Request MPIN Reset OTP
     * POST /api/v1/users/mpin/reset/request-otp
     * Header: identifier (email or phone)
     */
    @PostMapping("/mpin/reset/request-otp")
    public ResponseEntity<ApiResponse<String>> requestMpinResetOtp(
            @RequestHeader("identifier") String identifier) {
        log.info(" MPIN reset OTP requested for: {}", identifier);
        userService.requestMpinResetOtp(identifier);
        return ResponseUtil.success("OTP sent successfully",
                "Please check your email for OTP to reset MPIN");
    }

    /**
     * Reset MPIN with OTP
     * POST /api/v1/users/mpin/reset/verify-otp
     * Header: identifier, otp
     */
    @PostMapping("/mpin/reset/verify-otp")
    public ResponseEntity<ApiResponse<UserResponse>> resetMpin(
            @RequestHeader("identifier") String identifier,
            @RequestHeader("otp") String otp,
            @Valid @RequestBody MpinResetRequest request) {
        log.info(" MPIN reset for user: {}", identifier);
        UserResponse response = userService.resetMpin(identifier, otp, request);
        return ResponseUtil.success("MPIN reset successfully", response);
    }

    // PROFILE MANAGEMENT

    /**
     * Get User Profile
     * GET /api/v1/users/profile
     * Header: internal-user-id
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @RequestHeader("internal-user-id") String internalUserId) {
        log.info(" Fetching profile for user: {}", internalUserId);
        UserResponse response = userService.getUserByInternalId(internalUserId);
        return ResponseUtil.success("Profile fetched successfully", response);
    }

    /**
     * Update: UserName
     * PUT /api/v1/users/profile/name
     * Header: internal-user-id
     */
    @PutMapping("/profile/name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateName(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody NameRegistrationRequest request) {
        log.info(" Updating name for user: {}", internalUserId);
        UserResponse response = userService.updateName(internalUserId, request);
        return ResponseUtil.success("Name updated successfully", response);
    }

    // ADDRESS MANAGEMENT

    /**
     * Add/Update Address
     * POST /api/v1/users/address
     * Header: internal-user-id
     */
    @PostMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> addOrUpdateAddress(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody AddressRequest request) {
        log.info(" Adding/Updating address for user: {}", internalUserId);
        AddressResponse response = userService.addOrUpdateAddress(internalUserId, request);
        return ResponseUtil.success("Address saved successfully", response);
    }

    /**
     * Get User Address
     * GET /api/v1/users/address
     * Header: internal-user-id
     */
    @GetMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @RequestHeader("internal-user-id") String internalUserId) {
        log.info(" Fetching address for user: {}", internalUserId);
        AddressResponse response = userService.getAddress(internalUserId);
        return ResponseUtil.success("Address fetched successfully", response);
    }

    //KYC MANAGEMENT

    /**
     * Submit KYC Documents
     * POST /api/v1/users/kyc/submit
     * Header: internal-user-id
     */
    @PostMapping("/kyc/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> submitKYC(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody KYCSubmissionRequest request) {
        log.info(" KYC submission for user: {}", internalUserId);
        UserResponse response = userService.submitKYC(internalUserId, request);
        return ResponseUtil.success("KYC documents submitted successfully", response);
    }

    /**
     * Get KYC Status
     * GET /api/v1/users/kyc/status
     * Header: internal-user-id
     */
    @GetMapping("/kyc/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getKYCStatus(
            @RequestHeader("internal-user-id") String internalUserId) {
        log.info(" Fetching KYC status for user: {}", internalUserId);
        KycStatusResponse response = userService.getKYCStatus(internalUserId);
        return ResponseUtil.success("KYC status fetched successfully", response);
    }

    /**
     * Verify KYC (Admin Only)
     * PATCH /api/v1/users/admin/kyc/{internalUserId}/verify
     */
    @PatchMapping("/admin/kyc/{internalUserId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> verifyKYC(
            @PathVariable String internalUserId) {
        log.info(" Admin verifying KYC for user: {}", internalUserId);
        UserResponse response = userService.approveKYC(internalUserId);
        return ResponseUtil.success("KYC verified successfully", response);
    }

    /**
     * Reject KYC (Admin Only)
     * PATCH /api/v1/users/admin/kyc/{internalUserId}/reject
     */
    @PatchMapping("/admin/kyc/{internalUserId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> rejectKYC(
            @PathVariable String internalUserId,
            @RequestParam String reason) {
        log.info(" Admin rejecting KYC for user: {}", internalUserId);
        UserResponse response = userService.rejectKYC(internalUserId, reason);
        return ResponseUtil.success("KYC rejected", response);
    }

    /**
     * Unblock User (Admin Only)
     * PATCH /api/v1/users/admin/{internalUserId}/unblock
     */
    @PatchMapping("/admin/{internalUserId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> unblockUser(
            @PathVariable String internalUserId) {
        log.info(" Admin unblocking user: {}", internalUserId);
        userService.unblockUser(internalUserId);
        return ResponseUtil.success("User unblocked successfully",
                "MPIN lock has been removed. User can now login.");
    }

    //HELPER METHODS

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
