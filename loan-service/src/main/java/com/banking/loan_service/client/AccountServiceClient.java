package com.banking.loan_service.client;

import com.banking.loan_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountServiceClient {

    @PostMapping("/api/v1/transactions")
    ApiResponse<TransactionResponse> processTransaction(@RequestBody TransactionRequest request);
}