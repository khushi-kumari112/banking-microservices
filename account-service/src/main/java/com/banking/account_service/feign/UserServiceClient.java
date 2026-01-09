package com.banking.account_service.feign;

import com.banking.account_service.config.FeignClientConfig;
import com.banking.account_service.dto.UserVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client to communicate with User Service
 *

 */
@FeignClient(
        name = "user-service",
        url = "${user.service.url}",
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {

    /**
     * Verify if user exists and get KYC status
     * Called before creating account
     */
    @GetMapping("/api/v1/users/verify/{internalUserId}")
    UserVerificationResponse verifyUser(@PathVariable("internalUserId") String internalUserId);

    /**
     * Get user's basic details
     * Used for account statements, notifications
     */
    @GetMapping("/api/v1/users/internal/{internalUserId}")
    UserVerificationResponse getUserDetails(@PathVariable("internalUserId") String internalUserId);
}
