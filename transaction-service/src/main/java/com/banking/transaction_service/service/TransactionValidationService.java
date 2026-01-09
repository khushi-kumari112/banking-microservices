package com.banking.transaction_service.service;

import com.banking.transaction_service.dto.AccountBalanceResponse;
import com.banking.transaction_service.dto.ApiResponse;
import com.banking.transaction_service.enums.TransferMode;
import com.banking.transaction_service.exception.custom.*;
import com.banking.transaction_service.feign.AccountServiceClient;
import com.banking.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction Validation Service
 * Handles all validation logic for transactions
 * - Account validation
 * - Transfer limits
 * - Daily limits
 *
 * Note: Exceptions are handled by GlobalExceptionHandler
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionValidationService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;

    @Value("${transaction.limit.daily}")
    private BigDecimal dailyLimit;

    @Value("${transaction.limit.per-transaction}")
    private BigDecimal perTransactionLimit;

    @Value("${transaction.transfer.rtgs.min-amount}")
    private BigDecimal rtgsMinAmount;

    /**
     * Validates if account exists and is active
     * @throws AccountNotFoundException if account not found
     * @throws InvalidTransactionException if account is not active
     */
    public void validateAccount(Long accountId) {
        log.debug("Validating account: {}", accountId);

        ApiResponse<AccountBalanceResponse> response = accountServiceClient.getAccountBalance(accountId);

        if (response == null || response.getData() == null) {
            log.error("Account not found: {}", accountId);
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        String status = response.getData().getAccountStatus();
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            log.error("Account {} is not active. Status: {}", accountId, status);
            throw new InvalidTransactionException("Account is not active: " + accountId);
        }

        log.debug("Account {} validated successfully", accountId);
    }

    /**
     * Validates transfer between two accounts
     * @throws InvalidTransactionException if same account or accounts invalid
     */
    public void validateTransferAccounts(Long fromAccountId, Long toAccountId) {
        if (fromAccountId.equals(toAccountId)) {
            log.error("Attempted transfer to same account: {}", fromAccountId);
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        validateAccount(fromAccountId);
        validateAccount(toAccountId);
    }

    /**
     * Validates transfer mode and amount limits
     * @throws TransactionLimitExceededException if amount exceeds limits
     * @throws InvalidTransactionException if RTGS minimum not met
     */
    public void validateTransferTypeAndAmount(TransferMode transferMode, BigDecimal amount) {
        // Per-transaction limit check
        if (amount.compareTo(perTransactionLimit) > 0) {
            log.error("Amount {} exceeds per-transaction limit {}", amount, perTransactionLimit);
            throw new TransactionLimitExceededException(
                    "Amount exceeds per-transaction limit of ₹" + perTransactionLimit);
        }

        // RTGS minimum amount validation
        if (transferMode == TransferMode.RTGS && amount.compareTo(rtgsMinAmount) < 0) {
            log.error("RTGS amount {} is less than minimum {}", amount, rtgsMinAmount);
            throw new InvalidTransactionException(
                    "RTGS requires minimum amount of ₹" + rtgsMinAmount);
        }

        log.debug("Transfer mode {} and amount {} validated", transferMode, amount);
    }

    /**
     * Checks if adding amount would exceed daily limit
     * @throws TransactionLimitExceededException if daily limit exceeded
     */
    public void checkDailyLimit(Long accountId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        BigDecimal dailyTotal = transactionRepository.calculateDailyDebitTotal(
                accountId, startOfDay, endOfDay);

        if (dailyTotal == null) {
            dailyTotal = BigDecimal.ZERO;
        }

        BigDecimal newTotal = dailyTotal.add(amount);

        if (newTotal.compareTo(dailyLimit) > 0) {
            log.error("Daily limit exceeded for account {}. Current: {}, Attempted: {}, Limit: {}",
                    accountId, dailyTotal, amount, dailyLimit);
            throw new TransactionLimitExceededException(
                    String.format("Daily transaction limit of ₹%s exceeded. Current total: ₹%s",
                            dailyLimit, dailyTotal));
        }

        log.debug("Daily limit check passed for account {}. Total: {}", accountId, newTotal);
    }
}