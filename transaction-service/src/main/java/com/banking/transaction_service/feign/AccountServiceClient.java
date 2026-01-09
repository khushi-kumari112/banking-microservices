package com.banking.transaction_service.feign;

import com.banking.transaction_service.config.FeignConfig;
import com.banking.transaction_service.dto.AccountBalanceResponse;
import com.banking.transaction_service.dto.ApiResponse;
import com.banking.transaction_service.dto.BalanceUpdateRequest;
import com.banking.transaction_service.exception.custom.AccountServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "account-service",
        url = "${account.service.url}",
        configuration = FeignConfig.class
)
public interface AccountServiceClient {

    @GetMapping("/api/v1/account/{accountId}/balance")
    @CircuitBreaker(name = "account-service", fallbackMethod = "getBalanceFallback")
    @Retry(name = "account-service")
    ApiResponse<AccountBalanceResponse> getAccountBalance(@PathVariable("accountId") Long accountId);

    @GetMapping("/api/v1/account/get-account/{accountNumber}")
    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountFallback")
    @Retry(name = "account-service")
    ApiResponse<AccountBalanceResponse> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    //  For updating balance
    @PutMapping("/api/v1/account/{accountId}/balance")
    @CircuitBreaker(name = "account-service", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "account-service")
    ApiResponse<String> updateBalance(
            @PathVariable("accountId") Long accountId,
            @RequestBody BalanceUpdateRequest request);

    // For credit operations (deposits)
    @PostMapping("/api/v1/account/{accountId}/credit")
    @CircuitBreaker(name = "account-service", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "account-service")
    ApiResponse<String> credit(
            @PathVariable("accountId") Long accountId,
            @RequestBody BalanceUpdateRequest request);

    //  For debit operations (withdrawals)
    @PostMapping("/api/v1/account/{accountId}/debit")
    @CircuitBreaker(name = "account-service", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "account-service")
    ApiResponse<String> debit(
            @PathVariable("accountId") Long accountId,
            @RequestBody BalanceUpdateRequest request);

    // Fallback methods
    default ApiResponse<AccountBalanceResponse> getBalanceFallback(Long accountId, Exception e) {
        throw new AccountServiceUnavailableException("Account service unavailable");
    }

    default ApiResponse<AccountBalanceResponse> getAccountFallback(String accountNumber, Exception e) {
        throw new AccountServiceUnavailableException("Account service unavailable");
    }

    default ApiResponse<String> updateBalanceFallback(
            Long accountId, BalanceUpdateRequest request, Exception e) {
        throw new AccountServiceUnavailableException("Account service unavailable");
    }
}