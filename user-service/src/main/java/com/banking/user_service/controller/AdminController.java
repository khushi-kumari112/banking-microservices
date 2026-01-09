
package com.banking.user_service.controller;

import com.banking.user_service.dto.*;
import com.banking.user_service.service.AdminService;
import com.banking.user_service.util.ApiResponse;
import com.banking.user_service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for Banking Application
 * Handles admin-specific operations like admin registration
 *
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    /**
     * Register Admin User
     * POST /api/v1/admin/register
     *
     * This endpoint should be protected in production (only super-admin access)
     * For demo/testing purposes, it's open
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AdminRegistrationResponse>> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest request,
            @RequestHeader(value = "admin-secret-key", required = true) String secretKey) {

        log.info(" Admin registration initiated for email: {}", request.getEmail());

        AdminRegistrationResponse response = adminService.registerAdmin(request, secretKey);

        return ResponseUtil.success("Admin registered successfully", response);
    }

    /**
     * Get All Admins (Super Admin Only)
     * GET /api/v1/admin/list
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Object>> getAllAdmins() {
        log.info(" Fetching all admin users");
        // Implementation can be added later
        return ResponseUtil.success("Feature coming soon", null);
    }
}