package com.banking.account_service.service;

import com.banking.account_service.dto.*;
import com.banking.account_service.entity.Account;
import com.banking.account_service.enums.AccountStatus;
import com.banking.account_service.enums.AccountType;
import com.banking.account_service.enums.AuditAction;
import com.banking.account_service.exception.custom.AccountNotFoundException;
import com.banking.account_service.feign.UserServiceClient;
import com.banking.account_service.repository.AccountRepository;
import com.banking.account_service.util.AccountMapper;
import com.banking.account_service.util.AccountNumberGenerator;
import com.banking.account_service.util.IFSCCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Account Service Implementation - Production Ready
 *
 * Features:
 * Real-world banking validations
 * KYC verification
 * Account limits enforcement
 * Audit trail logging
 * Redis caching
 * Circuit breaker for User Service
 * Transaction management
 *

 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;
    private final AccountNumberGenerator accountNumberGenerator;
    private final IFSCCodeGenerator ifscCodeGenerator;
    private final AccountMapper accountMapper;
    private final AccountValidationService validationService;
    private final AccountAuditService auditService;

    /**
     * Create a new bank account
     *
     * Real Banking Flow:
     * 1. Verify user exists & get KYC status (User Service)
     * 2. Validate KYC is verified
     * 3. Check account limits
     * 4. Generate account number (14 digits)
     * 5. Generate IFSC code
     * 6. Create account with initial balance = 0
     * 7. Log audit trail
     */
    @Override
    @CacheEvict(value = "accounts", key = "#request.internalUserId")
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info(" Creating {} account for user: {}",
                request.getAccountType(), request.getInternalUserId());

        // STEP 1: Verify user & get KYC status from User Service
        UserVerificationResponse user = userServiceClient.verifyUser(
                request.getInternalUserId()
        );
        log.info(" User verified: {} | KYC: {} | Active: {}",
                user.getFullName(), user.getKycStatus(), user.isActive());

        // STEP 2: Validate account creation rules
        validationService.validateAccountCreation(user, request.getAccountType());

        // STEP 3: Generate unique account number
        String accountNumber = generateUniqueAccountNumber();
        log.info(" Generated account number: {}", accountNumber);

        // STEP 4: Generate IFSC code
        String ifscCode = ifscCodeGenerator.generateIFSCCode(request.getBranchCode());
        log.info(" Generated IFSC code: {}", ifscCode);

        // STEP 5: Create account entity
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setInternalUserId(request.getInternalUserId());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setIfscCode(ifscCode);
        account.setBranchCode(request.getBranchCode());
        account.setBranchName(request.getBranchName());
        account.setCurrency("INR");
        account.setCreatedDate(LocalDateTime.now());
        account.setLastModifiedDate(LocalDateTime.now());

        // STEP 6: Save to database
        Account savedAccount = accountRepository.save(account);

        // STEP 7: Log audit
        auditService.logAccountCreation(
                accountNumber,
                request.getInternalUserId(),
                request.getAccountType().toString()
        );

        log.info(" Account created successfully: {} for user: {}",
                accountNumber, request.getInternalUserId());

        return accountMapper.toResponse(savedAccount);
    }

    /**
     * Get account by account number (with Redis caching)
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info(" Fetching account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with account number: " + accountNumber
                ));

        return accountMapper.toResponse(account);
    }

    /**
     * Get all accounts for a user
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user-accounts", key = "#internalUserId")
    public List<AccountResponse> getAccountsByUserId(String internalUserId) {
        log.info(" Fetching all accounts for user: {}", internalUserId);

        List<Account> accounts = accountRepository.findByInternalUserId(internalUserId);

        if (accounts.isEmpty()) {
            log.warn(" No accounts found for user: {}", internalUserId);
            throw new AccountNotFoundException(
                    "No accounts found for user: " + internalUserId
            );
        }

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get only active accounts for a user
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getActiveAccountsByUserId(String internalUserId) {
        log.info(" Fetching active accounts for user: {}", internalUserId);

        List<Account> accounts = accountRepository
                .findByInternalUserIdAndStatus(internalUserId, AccountStatus.ACTIVE);

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by user ID and type
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserIdAndType(
            String internalUserId,
            AccountType accountType
    ) {
        log.info(" Fetching {} accounts for user: {}", accountType, internalUserId);

        List<Account> accounts = accountRepository
                .findByInternalUserIdAndAccountType(internalUserId, accountType);

        return accounts.stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get account balance
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "balance", key = "#accountNumber")
    public BigDecimal getAccountBalance(String accountNumber) {
        log.info(" Fetching balance for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber
                ));

        return account.getBalance();
    }

    /**
     * Update account balance (Called by Transaction Service)
     *
     * Real Banking: This is called by Transaction Service after successful transaction
     * Operations:
     * - CREDIT: Deposit, Transfer In, Salary Credit
     * - DEBIT: Withdrawal, Transfer Out, Bill Payment
     */
    @Override
    @CacheEvict(value = {"accounts", "balance"}, key = "#request.accountNumber")
    public AccountResponse updateBalance(BalanceUpdateRequest request) {
        log.info(" Updating balance | Account: {} | Operation: {} | Amount: {}",
                request.getAccountNumber(), request.getOperation(), request.getAmount());

        // STEP 1: Find account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + request.getAccountNumber()
                ));

        // STEP 2: Validate operation
        validationService.validateBalanceOperation(
                account,
                request.getAmount(),
                request.getOperation()
        );

        // STEP 3: Get current balance
        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance;

        // STEP 4: Perform operation
        switch (request.getOperation()) {
            case "CREDIT":
                account.credit(request.getAmount());
                newBalance = account.getBalance();
                log.info(" Credit: {} + {} = {}", oldBalance, request.getAmount(), newBalance);
                break;

            case "DEBIT":
                account.debit(request.getAmount());
                newBalance = account.getBalance();
                log.info(" Debit: {} - {} = {}", oldBalance, request.getAmount(), newBalance);
                break;

            default:
                throw new IllegalArgumentException("Invalid operation: " + request.getOperation());
        }

        // STEP 5: Save
        Account updatedAccount = accountRepository.save(account);

        // STEP 6: Log audit
        auditService.logBalanceUpdate(
                request.getAccountNumber(),
                oldBalance.toString(),
                newBalance.toString(),
                request.getOperation()
        );

        log.info(" Balance updated successfully for account: {}", request.getAccountNumber());

        return accountMapper.toResponse(updatedAccount);
    }

    /**
     * Update account status
     */
    @Override
    @CacheEvict(value = {"accounts", "user-accounts"}, allEntries = true)
    public AccountResponse updateAccountStatus(AccountStatusUpdateRequest request) {
        log.info(" Updating status | Account: {} | New Status: {}",
                request.getAccountNumber(), request.getStatus());

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + request.getAccountNumber()
                ));

        // Validate status change
        AccountStatus oldStatus = account.getStatus();
        validationService.validateStatusChange(oldStatus, request.getStatus());

        // Update status
        account.setStatus(request.getStatus());
        account.setLastModifiedDate(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        // Log audit
        auditService.logStatusChange(
                request.getAccountNumber(),
                oldStatus.toString(),
                request.getStatus().toString(),
                request.getReason()
        );

        log.info(" Account status updated: {} -> {}", oldStatus, request.getStatus());

        return accountMapper.toResponse(updatedAccount);
    }

    /**
     * Close account
     *
     * Real Banking Rules:
     * - Balance must be zero
     * - Cannot close already closed account
     * - Cannot close blocked account directly
     */
    @Override
    @CacheEvict(value = {"accounts", "user-accounts"}, allEntries = true)
    public void closeAccount(String accountNumber, String reason) {
        log.info(" Closing account: {} | Reason: {}", accountNumber, reason);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber
                ));

        // Validate closure
        validationService.validateAccountClosure(account);

        // Close account
        account.setStatus(AccountStatus.CLOSED);
        account.setClosureDate(LocalDateTime.now());
        account.setClosureReason(reason != null ? reason : "Customer request");
        account.setLastModifiedDate(LocalDateTime.now());

        accountRepository.save(account);

        // Log audit
        auditService.logAudit(
                accountNumber,
                account.getInternalUserId(),
                AuditAction.CLOSE_ACCOUNT,
                "Account closed: " + reason,
                AccountStatus.ACTIVE.toString(),
                AccountStatus.CLOSED.toString(),
                "ADMIN",
                null
        );

        log.info(" Account closed successfully: {}", accountNumber);
    }

    /**
     * Count active accounts for a user
     */
    @Override
    @Transactional(readOnly = true)
    public long countActiveAccounts(String internalUserId) {
        log.info(" Counting active accounts for user: {}", internalUserId);
        return accountRepository.countByInternalUserIdAndStatus(
                internalUserId,
                AccountStatus.ACTIVE
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Generate unique account number with collision handling
     */
    private String generateUniqueAccountNumber() {
        String accountNumber = accountNumberGenerator.generateAccountNumber();

        // Handle collisions (rare but possible)
        int retries = 0;
        while (accountRepository.existsByAccountNumber(accountNumber) && retries < 5) {
            log.warn(" Account number collision detected. Regenerating...");
            accountNumber = accountNumberGenerator.generateAccountNumber();
            retries++;
        }

        if (retries >= 5) {
            throw new RuntimeException(
                    "Unable to generate unique account number after 5 attempts. Please try again."
            );
        }

        return accountNumber;
    }
}