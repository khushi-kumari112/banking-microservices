package com.banking.user_service.service;

import com.banking.user_service.dto.AdminRegistrationRequest;
import com.banking.user_service.dto.AdminRegistrationResponse;

/**
 * Admin Service Interface
 * Handles admin-specific operations
 */
public interface AdminService {
    AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request, String secretKey);
}