// ═══════════════════════════════════════════════════════════════════════
// File: AccountValidationService.java
// ═══════════════════════════════════════════════════════════════════════
package com.banking.account_service.service;

import com.banking.account_service.config.AppProperties;
import com.banking.account_service.dto.UserVerificationResponse;
import com.banking.account_service.entity.Account;
import com.banking.account_service.enums.AccountStatus;
import com.banking.account_service.enums.AccountType;
import com.banking.account_service.exception.custom.*;
import com.banking.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Account Validation Service
 *
 * Implements real-world banking business rules:
 * - RBI compliance checks
 * - KYC verification
 * - Account limits
 * - Transaction limits
 * - Fraud detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountValidationService {

    private final AccountRepository accountRepository;
    private final AppProperties appProperties;

    /**
     * Validate user can create account
     * Real Banking Rules:
     * 1. KYC must be verified
     * 2. User must be active
     * 3. Cannot exceed max accounts limit
     * 4. Cannot exceed account type limit
     */
    public void validateAccountCreation(
            UserVerificationResponse user,
            AccountType accountType
    ) {
        // Rule 1: KYC must be verified
        if (!user.isKycVerified()) {
            log.error(" KYC not verified for user: {}", user.getInternalUserId());
            throw new KycNotVerifiedException(
                    "Cannot create account. KYC verification pending. Please complete KYC first."
            );
        }

        // Rule 2: User must be active
        if (!user.isActive()) {
            log.error(" User is inactive: {}", user.getInternalUserId());
            throw new InvalidAccountOperationException(
                    "Cannot create account. User account is inactive."
            );
        }

        // Rule 3: Check max accounts per user
        long totalAccounts = accountRepository.countByInternalUserIdAndStatus(
                user.getInternalUserId(),
                AccountStatus.ACTIVE
        );

        if (totalAccounts >= appProperties.getMaxPerUser()) {
            log.error(" Max accounts limit exceeded for user: {}", user.getInternalUserId());
            throw new AccountLimitExceededException(
                    String.format("Cannot create more than %d accounts per user",
                            appProperties.getMaxPerUser())
            );
        }

        // Rule 4: Check account type specific limits
        validateAccountTypeLimit(user.getInternalUserId(), accountType);

        log.info(" Account creation validation passed for user: {}", user.getInternalUserId());
    }

    /**
     * Validate account type specific limits
     * Real Banking: Max 2 savings, 1 current per user
     */
    private void validateAccountTypeLimit(String internalUserId, AccountType accountType) {
        long typeCount = accountRepository.countByInternalUserIdAndAccountType(
                internalUserId,
                accountType
        );

        switch (accountType) {
            case SAVINGS:
                if (typeCount >= appProperties.getMaxSavingsPerUser()) {
                    throw new AccountLimitExceededException(
                            String.format("Cannot create more than %d savings accounts",
                                    appProperties.getMaxSavingsPerUser())
                    );
                }
                break;

            case CURRENT:
                if (typeCount >= appProperties.getMaxCurrentPerUser()) {
                    throw new AccountLimitExceededException(
                            String.format("Cannot create more than %d current accounts",
                                    appProperties.getMaxCurrentPerUser())
                    );
                }
                break;

            case SALARY:
            case WALLET:
                // No specific limits for these types
                break;
        }
    }

    /**
     * Validate balance operation
     * Checks: sufficient balance, daily limits, minimum balance
     */
    public void validateBalanceOperation(
            Account account,
            BigDecimal amount,
            String operation
    ) {
        // Rule 1: Account must be active
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException(
                    "Cannot perform operation. Account is " + account.getStatus()
            );
        }

        // Rule 2: Amount must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAccountOperationException(
                    "Amount must be greater than zero"
            );
        }

        if ("DEBIT".equals(operation)) {
            validateDebitOperation(account, amount);
        } else if ("CREDIT".equals(operation)) {
            validateCreditOperation(account, amount);
        }
    }

    /**
     * Validate debit operation
     */
    private void validateDebitOperation(Account account, BigDecimal amount) {
        BigDecimal currentBalance = account.getBalance();

        // Check sufficient balance
        if (currentBalance.compareTo(amount) < 0) {
            log.error(" Insufficient balance. Available: {}, Required: {}",
                    currentBalance, amount);
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %s, Required: %s",
                            currentBalance, amount)
            );
        }

        // Check minimum balance requirement
        BigDecimal minBalance = getMinimumBalance(account.getAccountType());
        BigDecimal balanceAfterDebit = currentBalance.subtract(amount);

        if (balanceAfterDebit.compareTo(minBalance) < 0) {
            log.error(" Minimum balance violation. Min required: {}", minBalance);
            throw new InvalidAccountOperationException(
                    String.format("Cannot maintain minimum balance of %s after this operation",
                            minBalance)
            );
        }

        // Check daily withdrawal limit
        BigDecimal dailyLimit = getDailyWithdrawalLimit(account.getAccountType());
        if (amount.compareTo(dailyLimit) > 0) {
            log.error(" Daily withdrawal limit exceeded. Limit: {}", dailyLimit);
            throw new DailyLimitExceededException(
                    String.format("Daily withdrawal limit of %s exceeded", dailyLimit)
            );
        }
    }

    /**
     * Validate credit operation
     */
    private void validateCreditOperation(Account account, BigDecimal amount) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal maxBalance = getMaximumBalance(account.getAccountType());
        BigDecimal balanceAfterCredit = currentBalance.add(amount);

        // Check maximum balance limit
        if (balanceAfterCredit.compareTo(maxBalance) > 0) {
            log.error(" Maximum balance limit exceeded. Max: {}", maxBalance);
            throw new InvalidAccountOperationException(
                    String.format("Cannot exceed maximum balance of %s", maxBalance)
            );
        }
    }

    /**
     * Get minimum balance based on account type
     */
    private BigDecimal getMinimumBalance(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> appProperties.getSavings().getMinBalance();
            case CURRENT -> appProperties.getCurrent().getMinBalance();
            case SALARY -> appProperties.getSalary().getMinBalance();
            case WALLET -> appProperties.getWallet().getMinBalance();
        };
    }

    /**
     * Get maximum balance based on account type
     */
    private BigDecimal getMaximumBalance(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> appProperties.getSavings().getMaxBalance();
            case CURRENT -> appProperties.getCurrent().getMaxBalance();
            case SALARY -> appProperties.getSalary().getMaxBalance();
            case WALLET -> appProperties.getWallet().getMaxBalance();
        };
    }

    /**
     * Get daily withdrawal limit based on account type
     */
    private BigDecimal getDailyWithdrawalLimit(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> appProperties.getSavings().getDailyWithdrawalLimit();
            case CURRENT -> appProperties.getCurrent().getDailyWithdrawalLimit();
            case SALARY -> appProperties.getSalary().getDailyWithdrawalLimit();
            case WALLET -> appProperties.getWallet().getDailyTransactionLimit();
        };
    }

    /**
     * Validate account status change
     */
    public void validateStatusChange(AccountStatus currentStatus, AccountStatus newStatus) {
        // Cannot reactivate a closed account
        if (currentStatus == AccountStatus.CLOSED && newStatus == AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException(
                    "Cannot activate a closed account. Please create a new account."
            );
        }

        // Cannot close a blocked account directly
        if (currentStatus == AccountStatus.BLOCKED && newStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountOperationException(
                    "Cannot close a blocked account. Please unblock first."
            );
        }
    }

    /**
     * Validate account closure
     */
    public void validateAccountClosure(Account account) {
        // Cannot close already closed account
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountOperationException("Account is already closed");
        }

        // Balance must be zero
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidAccountOperationException(
                    String.format("Cannot close account with non-zero balance. Current balance: %s",
                            account.getBalance())
            );
        }
    }
}