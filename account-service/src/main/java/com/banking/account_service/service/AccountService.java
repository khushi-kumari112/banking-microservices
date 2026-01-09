package com.banking.account_service.service;

import com.banking.account_service.dto.*;
import com.banking.account_service.enums.AccountType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account Service Interface
 * Defines business operations for account management
 *
 */
public interface AccountService {

    /**
     * Create a new bank account
     * Validates user exists, generates account number & IFSC
     */
    AccountResponse createAccount(CreateAccountRequest request);

    /**
     * Get account by account number
     * Throws AccountNotFoundException if account doesn't exist
     */
    AccountResponse getAccountByNumber(String accountNumber);

    /**
     * Get all accounts for a user (One user can have multiple accounts)
     */
    List<AccountResponse> getAccountsByUserId(String internalUserId);

    /**
     * Get only active accounts for a user
     */
    List<AccountResponse> getActiveAccountsByUserId(String internalUserId);

    /**
     * Get accounts by user ID and account type
     */
    List<AccountResponse> getAccountsByUserIdAndType(String internalUserId, AccountType accountType);

    /**
     * Get account balance
     */
    BigDecimal getAccountBalance(String accountNumber);

    /**
     * Update account balance (Credit/Debit)
     * Called by Transaction Service
     * Throws InsufficientBalanceException if debit amount > balance
     */
    AccountResponse updateBalance(BalanceUpdateRequest request);

    /**
     * Update account status
     * Throws InvalidAccountOperationException if operation not allowed
     */
    AccountResponse updateAccountStatus(AccountStatusUpdateRequest request);

    /**
     * Close account
     * Sets status to CLOSED and records closure reason
     */
    void closeAccount(String accountNumber, String reason);

    /**
     * Count active accounts for a user
     */
    long countActiveAccounts(String internalUserId);
}
