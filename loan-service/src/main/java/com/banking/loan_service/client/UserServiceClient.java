package com.banking.loan_service.client;

import com.banking.loan_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/profile")
    ApiResponse<UserResponse> getUserProfile(@RequestHeader("internal-user-id") String internalUserId);
}