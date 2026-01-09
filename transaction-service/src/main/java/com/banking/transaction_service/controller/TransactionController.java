package com.banking.transaction_service.controller;

import com.banking.transaction_service.dto.*;
import com.banking.transaction_service.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction Controller
 * Handles all transaction-related REST endpoints
 *
 * Integration:
 * - User Service: Via JWT (userId & username from token)
 * - Account Service: Via Feign Client (balance checks & updates)
 * - Redis: Idempotency
 * - Kafka: Event publishing
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Process money transfer (IMPS/NEFT/RTGS)
     * POST /api/v1/transactions/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Long userId = extractUserId(httpRequest);
        String username = authentication.getName();

        log.info(" Transfer request | User: {} (ID: {}) | From: {} | To: {} | Amount: ₹{}",
                username, userId, request.getFromAccountId(), request.getToAccountNumber(), request.getAmount());

        TransactionResponse response = transactionService.transfer(request, userId, username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer initiated successfully", response));
    }

    /**
     * Process deposit
     * POST /api/v1/transactions/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Long userId = extractUserId(httpRequest);
        String username = authentication.getName();

        log.info(" Deposit request | User: {} (ID: {}) | Account: {} | Amount: ₹{}",
                username, userId, request.getAccountId(), request.getAmount());

        TransactionResponse response = transactionService.deposit(request, userId, username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit completed successfully", response));
    }

    /**
     * Process withdrawal
     * POST /api/v1/transactions/withdrawal
     */
    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawal(
            @Valid @RequestBody WithdrawalRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        Long userId = extractUserId(httpRequest);
        String username = authentication.getName();

        log.info(" Withdrawal request | User: {} (ID: {}) | Account: {} | Amount: ₹{}",
                username, userId, request.getAccountId(), request.getAmount());

        TransactionResponse response = transactionService.withdrawal(request, userId, username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal completed successfully", response));
    }

    /**
     * Get transaction by ID
     * GET /api/v1/transactions/{transactionId}
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionId) {

        log.info(" Fetching transaction: {}", transactionId);
        TransactionResponse response = transactionService.getTransaction(transactionId);

        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    /**
     * Get transaction history for an account
     * GET /api/v1/transactions/account/{accountId}/history?page=0&size=10
     */
    @GetMapping("/account/{accountId}/history")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(" Fetching transaction history | Account: {} | Page: {} | Size: {}",
                accountId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> history = transactionService.getTransactionHistory(accountId, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Retrieved %d transactions", history.getNumberOfElements()),
                history));
    }

    /**
     * Get transaction by reference number (UTR/RRN)
     * GET /api/v1/transactions/reference/{referenceNumber}
     */
    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @PathVariable String referenceNumber) {

        log.info(" Fetching transaction by reference: {}", referenceNumber);
        TransactionResponse response = transactionService.getTransactionByReference(referenceNumber);

        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    /**
     * Reverse a transaction (Admin only)
     * POST /api/v1/transactions/{transactionId}/reverse
     */
    @PostMapping("/{transactionId}/reverse")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @PathVariable String transactionId,
            @RequestParam String reason,
            Authentication authentication) {

        String username = authentication.getName();
        log.warn(" Transaction reversal requested | Transaction: {} | By: {} | Reason: {}",
                transactionId, username, reason);

        TransactionResponse response = transactionService.reverseTransaction(
                transactionId, reason, username);

        return ResponseEntity.ok(ApiResponse.success("Transaction reversed successfully", response));
    }

    /**
     * Health check endpoint (Public)
     * GET /api/v1/transactions/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("Health check requested");
        return ResponseEntity.ok(ApiResponse.success("Transaction Service is healthy", "UP"));
    }

    /**
     * Extract userId from JWT token stored in request attributes
     * This is set by JwtAuthenticationFilter
     */
    private Long extractUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        // Fallback: extract from header if needed
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            return Long.parseLong(userIdHeader);
        }
        throw new IllegalStateException("User ID not found in request");
    }
}