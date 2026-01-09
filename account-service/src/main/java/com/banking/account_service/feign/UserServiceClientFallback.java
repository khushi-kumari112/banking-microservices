package com.banking.account_service.feign;

import com.banking.account_service.dto.UserVerificationResponse;
import com.banking.account_service.exception.custom.UserServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback handler for User Service Feign Client
 *
 * Executes when:
 * - User Service is down
 * - Network timeout
 * - User Service returns 500 error
 *
 * Real-world scenario:
 * - If User Service crashes during account creation,
 *   this fallback prevents Account Service from crashing
 * - Returns meaningful error to user instead of generic 500 error

 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserVerificationResponse verifyUser(String internalUserId) {
        log.error("User Service is unavailable. Cannot verify user: {}", internalUserId);
        throw new UserServiceUnavailableException(
                "User Service is temporarily unavailable. Please try again later."
        );
    }

    @Override
    public UserVerificationResponse getUserDetails(String internalUserId) {
        log.error("User Service is unavailable. Cannot fetch user details: {}", internalUserId);
        throw new UserServiceUnavailableException(
                "User Service is temporarily unavailable. Please try again later."
        );
    }
}
