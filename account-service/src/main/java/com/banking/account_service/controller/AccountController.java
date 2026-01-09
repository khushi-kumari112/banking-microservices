package com.banking.account_service.controller;

import com.banking.account_service.dto.*;
import com.banking.account_service.enums.AccountType;
import com.banking.account_service.service.AccountService;
import com.banking.account_service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account Controller
 * Handles all account-related operations
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    /**
     * Create a new bank account
     * POST /api/v1/account/create-account
     */
    @PostMapping("/create-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @RequestHeader("internal-user-id") String internalUserId,
            @Valid @RequestBody CreateAccountRequest request) {

        log.info(" Request to create account for user: {}", internalUserId);

        // Set internal user ID from header into request
        request.setInternalUserId(internalUserId);

        AccountResponse response = accountService.createAccount(request);
        return ResponseUtil.success("Account created successfully", response);
    }

    /**
     * Get account by account number
     * GET /api/v1/account/get-account/{accountNumber}
     */
    @GetMapping("/get-account/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber
    ) {
        log.info("Request to fetch account: {}", accountNumber);
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseUtil.success(
                "Account fetched successfully",
                account,
                HttpStatus.OK
        );
    }

    /**
     * Get all accounts for a user
     * GET /api/v1/account/user-accounts/{internalUserId}
     */
    @GetMapping("/user-accounts/{internalUserId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getUserAccounts(
            @PathVariable String internalUserId
    ) {
        log.info("Request to fetch all accounts for user: {}", internalUserId);
        List<AccountResponse> accounts = accountService.getAccountsByUserId(internalUserId);
        return ResponseUtil.success(
                "Accounts fetched successfully",
                accounts,
                HttpStatus.OK
        );
    }

    /**
     * Get active accounts for a user
     * GET /api/v1/account/user-accounts/{internalUserId}/active
     */
    @GetMapping("/user-accounts/{internalUserId}/active")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getActiveAccounts(
            @PathVariable String internalUserId
    ) {
        log.info("Request to fetch active accounts for user: {}", internalUserId);
        List<AccountResponse> accounts = accountService.getActiveAccountsByUserId(internalUserId);
        return ResponseUtil.success(
                "Active accounts fetched successfully",
                accounts,
                HttpStatus.OK
        );
    }

    /**
     * Get accounts by type
     * GET /api/v1/account/user-accounts/{internalUserId}/type/{accountType}
     */
    @GetMapping("/user-accounts/{internalUserId}/type/{accountType}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByType(
            @PathVariable String internalUserId,
            @PathVariable AccountType accountType
    ) {
        log.info("Request to fetch {} accounts for user: {}", accountType, internalUserId);
        List<AccountResponse> accounts = accountService.getAccountsByUserIdAndType(
                internalUserId,
                accountType
        );
        return ResponseUtil.success(
                accountType + " accounts fetched successfully",
                accounts,
                HttpStatus.OK
        );
    }

    /**
     * Check account balance
     * GET /api/v1/account/balance/{accountNumber}
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<ApiResponse<BigDecimal>> checkBalance(
            @PathVariable String accountNumber
    ) {
        log.info("Request to check balance for account: {}", accountNumber);
        BigDecimal balance = accountService.getAccountBalance(accountNumber);
        return ResponseUtil.success(
                "Balance fetched successfully",
                balance,
                HttpStatus.OK
        );
    }

    /**
     * Update account balance (Called by Transaction Service)
     * PUT /api/v1/account/update-balance
     */
    @PutMapping("/update-balance")
    public ResponseEntity<ApiResponse<AccountResponse>> updateBalance(
            @Valid @RequestBody BalanceUpdateRequest request
    ) {
        log.info("Request to update balance for account: {}", request.getAccountNumber());
        AccountResponse account = accountService.updateBalance(request);
        return ResponseUtil.success(
                "Balance updated successfully",
                account,
                HttpStatus.OK
        );
    }

    /**
     * Update account status (ACTIVE, INACTIVE, BLOCKED, CLOSED)
     * PATCH /api/v1/account/update-status
     */
    @PatchMapping("/update-status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @Valid @RequestBody AccountStatusUpdateRequest request
    ) {
        log.info("Request to update status for account: {}", request.getAccountNumber());
        AccountResponse account = accountService.updateAccountStatus(request);
        return ResponseUtil.success(
                "Account status updated successfully",
                account,
                HttpStatus.OK
        );
    }

    /**
     * Close account
     * PATCH /api/v1/account/close-account/{accountNumber}
     */
    @PatchMapping("/close-account/{accountNumber}")
    public ResponseEntity<ApiResponse<String>> closeAccount(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String reason
    ) {
        log.info("Request to close account: {}", accountNumber);
        accountService.closeAccount(accountNumber, reason);
        return ResponseUtil.success(
                "Account closed successfully",
                "Account " + accountNumber + " has been closed",
                HttpStatus.OK
        );
    }

    /**
     * Count active accounts for user
     * GET /api/v1/account/user-accounts/{internalUserId}/count
     */
    @GetMapping("/user-accounts/{internalUserId}/count")
    public ResponseEntity<ApiResponse<Long>> countActiveAccounts(
            @PathVariable String internalUserId
    ) {
        log.info("Request to count active accounts for user: {}", internalUserId);
        long count = accountService.countActiveAccounts(internalUserId);
        return ResponseUtil.success(
                "Active accounts counted successfully",
                count,
                HttpStatus.OK
        );
    }

    /**
     * Health check endpoint
     * GET /api/v1/account/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseUtil.success(
                "Account Service is running",
                "Service is healthy",
                HttpStatus.OK
        );
    }
}
