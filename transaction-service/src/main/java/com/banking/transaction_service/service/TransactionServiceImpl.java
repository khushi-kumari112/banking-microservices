package com.banking.transaction_service.service;

import com.banking.transaction_service.config.AppProperties;
import com.banking.transaction_service.dto.*;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.enums.*;
import com.banking.transaction_service.exception.custom.*;
import com.banking.transaction_service.feign.AccountServiceClient;
import com.banking.transaction_service.kafka.TransactionEventProducer;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.util.TransactionMapper;
import com.banking.transaction_service.util.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Transaction Service Implementation
 * Handles money transfers, deposits, and withdrawals with:
 * - Idempotency support via Redis
 * - Real-time balance validation
 * - Transaction charges calculation
 * - Kafka event publishing
 * - Complete audit trail
 *

 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionEventProducer eventProducer;
    private final TransactionAuditService auditService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    private static final int DECIMAL_SCALE = 2;


    // TRANSFER OPERATION


    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request, Long userId, String username) {
        log.info("Transfer initiated | From: {} → To: {} | Amount: ₹{} | Mode: {} | By: {}",
                request.getFromAccountId(), request.getToAccountNumber(),
                request.getAmount(), request.getTransferMode(), username);

        // Check for duplicate transaction
        Optional<String> cachedTransactionId = checkIdempotency(request.getIdempotencyKey());
        if (cachedTransactionId.isPresent()) {
            log.warn(" Duplicate transaction detected | Key: {}", request.getIdempotencyKey());
            return getTransaction(cachedTransactionId.get());
        }

        // Validate business rules
        validateTransferLimits(request.getAmount(), request.getTransferMode());

        // Fetch and validate accounts
        AccountBalanceResponse sourceAccount = getAccountBalance(request.getFromAccountId());
        AccountBalanceResponse destinationAccount = getAccountByNumber(request.getToAccountNumber());

        validateAccountStatus(sourceAccount.getAccountStatus(), "Source");
        validateAccountStatus(destinationAccount.getAccountStatus(), "Destination");
        validateDifferentAccounts(request.getFromAccountId(), destinationAccount.getAccountId());

        // Calculate transaction costs
        BigDecimal charges = calculateTransferCharges(request.getAmount(), request.getTransferMode());
        BigDecimal tax = calculateGST(charges);
        BigDecimal totalAmount = request.getAmount().add(charges).add(tax);

        log.debug(" Breakdown | Amount: ₹{} | Charges: ₹{} | GST: ₹{} | Total: ₹{}",
                request.getAmount(), charges, tax, totalAmount);

        validateSufficientBalance(sourceAccount.getBalance(), totalAmount);

        // Create pending transaction record
        Transaction transaction = buildTransaction(request, sourceAccount, destinationAccount,
                charges, tax, totalAmount, username);

        Transaction savedTransaction = transactionRepository.save(transaction);
        auditService.logTransactionInitiated(savedTransaction.getTransactionId(), username);

        // Execute money transfer
        executeMoneyTransfer(savedTransaction, sourceAccount, destinationAccount, request.getAmount(), totalAmount);

        // Finalize transaction
        return finalizeTransaction(savedTransaction, username);
    }


    // DEPOSIT OPERATION


    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request, Long userId, String username) {
        log.info(" Deposit initiated | Account: {} | Amount: ₹{} | By: {}",
                request.getAccountId(), request.getAmount(), username);

        Optional<String> cachedTransactionId = checkIdempotency(request.getIdempotencyKey());
        if (cachedTransactionId.isPresent()) {
            return getTransaction(cachedTransactionId.get());
        }

        AccountBalanceResponse account = getAccountBalance(request.getAccountId());
        validateAccountStatus(account.getAccountStatus(), "Deposit");

        Transaction transaction = Transaction.builder()
                .transactionId(TransactionIdGenerator.generateTransactionId())
                .referenceNumber(TransactionIdGenerator.generateReferenceNumber())
                .transactionType(TransactionType.DEPOSIT)
                .toAccountId(request.getAccountId())
                .toAccountNumber(account.getAccountNumber())
                .amount(request.getAmount())
                .totalAmount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .initiatedBy(username)
                .idempotencyKey(request.getIdempotencyKey())
                .createdDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        auditService.logTransactionInitiated(savedTransaction.getTransactionId(), username);

        // Credit the account
        updateAccountBalance(request.getAccountId(), request.getAmount(),
                "CREDIT", savedTransaction.getTransactionId(), "Deposit");

        return finalizeTransaction(savedTransaction, username);
    }


    // WITHDRAWAL OPERATION


    @Override
    @Transactional
    public TransactionResponse withdrawal(WithdrawalRequest request, Long userId, String username) {
        log.info(" Withdrawal initiated | Account: {} | Amount: ₹{} | By: {}",
                request.getAccountId(), request.getAmount(), username);

        Optional<String> cachedTransactionId = checkIdempotency(request.getIdempotencyKey());
        if (cachedTransactionId.isPresent()) {
            return getTransaction(cachedTransactionId.get());
        }

        AccountBalanceResponse account = getAccountBalance(request.getAccountId());
        validateAccountStatus(account.getAccountStatus(), "Withdrawal");
        validateSufficientBalance(account.getBalance(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .transactionId(TransactionIdGenerator.generateTransactionId())
                .referenceNumber(TransactionIdGenerator.generateReferenceNumber())
                .transactionType(TransactionType.WITHDRAWAL)
                .fromAccountId(request.getAccountId())
                .fromAccountNumber(account.getAccountNumber())
                .amount(request.getAmount())
                .totalAmount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .initiatedBy(username)
                .idempotencyKey(request.getIdempotencyKey())
                .createdDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        auditService.logTransactionInitiated(savedTransaction.getTransactionId(), username);

        // Debit the account
        updateAccountBalance(request.getAccountId(), request.getAmount(),
                "DEBIT", savedTransaction.getTransactionId(), "Withdrawal");

        return finalizeTransaction(savedTransaction, username);
    }


    // QUERY OPERATIONS


    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        log.debug(" Fetching transaction: {}", transactionId);
        return transactionRepository.findByTransactionId(transactionId)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new InvalidTransactionException(
                        "Transaction not found: " + transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        log.debug(" Fetching transaction by reference: {}", referenceNumber);
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .map(TransactionMapper::toResponse)
                .orElseThrow(() -> new InvalidTransactionException(
                        "Transaction not found with reference: " + referenceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(Long accountId, Pageable pageable) {
        log.debug(" Fetching transaction history | Account: {} | Page: {}",
                accountId, pageable.getPageNumber());
        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedDateDesc(accountId, accountId, pageable)
                .map(TransactionMapper::toResponse);
    }


    // REVERSAL OPERATION

    @Override
    @Transactional
    public TransactionResponse reverseTransaction(String transactionId, String reason, String username) {
        log.info(" Reversal initiated | Transaction: {} | Reason: {} | By: {}",
                transactionId, reason, username);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new InvalidTransactionException(
                        "Transaction not found: " + transactionId));

        validateReversalEligibility(transaction);

        if (transaction.getTransactionType() == TransactionType.TRANSFER) {
            reverseTransfer(transaction);
        }

        transaction.setStatus(TransactionStatus.REVERSED);
        transaction.setReversedDate(LocalDateTime.now());
        transaction.setReversalReason(reason);
        transactionRepository.save(transaction);

        eventProducer.publishTransactionReversed(transaction);
        auditService.logTransactionReversed(transactionId, reason);

        log.info(" Transaction reversed successfully: {}", transactionId);
        return TransactionMapper.toResponse(transaction);
    }


    // PRIVATE HELPER METHODS


    private Optional<String> checkIdempotency(String idempotencyKey) {
        if (idempotencyKey == null) {
            return Optional.empty();
        }
        String cachedTransactionId = redisTemplate.opsForValue()
                .get(IDEMPOTENCY_KEY_PREFIX + idempotencyKey);
        return Optional.ofNullable(cachedTransactionId);
    }

    private void cacheIdempotencyKey(String idempotencyKey, String transactionId) {
        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set(
                    IDEMPOTENCY_KEY_PREFIX + idempotencyKey,
                    transactionId,
                    appProperties.getIdempotencyTtl(),
                    TimeUnit.SECONDS
            );
        }
    }

    private AccountBalanceResponse getAccountBalance(Long accountId) {
        return accountServiceClient.getAccountBalance(accountId).getData();
    }

    private AccountBalanceResponse getAccountByNumber(String accountNumber) {
        return accountServiceClient.getAccountByNumber(accountNumber).getData();
    }

    private Transaction buildTransaction(TransferRequest request,
                                         AccountBalanceResponse sourceAccount,
                                         AccountBalanceResponse destinationAccount,
                                         BigDecimal charges, BigDecimal tax,
                                         BigDecimal totalAmount, String username) {
        return Transaction.builder()
                .transactionId(TransactionIdGenerator.generateTransactionId())
                .referenceNumber(TransactionIdGenerator.generateReferenceNumber())
                .transactionType(TransactionType.TRANSFER)
                .transferMode(request.getTransferMode())
                .fromAccountId(request.getFromAccountId())
                .fromAccountNumber(sourceAccount.getAccountNumber())
                .toAccountId(destinationAccount.getAccountId())
                .toAccountNumber(destinationAccount.getAccountNumber())
                .amount(request.getAmount())
                .chargesAmount(charges)
                .taxAmount(tax)
                .totalAmount(totalAmount)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .initiatedBy(username)
                .idempotencyKey(request.getIdempotencyKey())
                .createdDate(LocalDateTime.now())
                .build();
    }

    private void executeMoneyTransfer(Transaction transaction,
                                      AccountBalanceResponse sourceAccount,
                                      AccountBalanceResponse destinationAccount,
                                      BigDecimal transferAmount,
                                      BigDecimal totalAmount) {
        // Debit from source (including charges)
        updateAccountBalance(transaction.getFromAccountId(), totalAmount, "DEBIT",
                transaction.getTransactionId(),
                "Transfer to " + destinationAccount.getAccountNumber());

        // Credit to destination (only transfer amount)
        updateAccountBalance(transaction.getToAccountId(), transferAmount, "CREDIT",
                transaction.getTransactionId(),
                "Transfer from " + sourceAccount.getAccountNumber());
    }

    private void updateAccountBalance(Long accountId, BigDecimal amount,
                                      String type, String transactionId,
                                      String description) {
        BalanceUpdateRequest request = BalanceUpdateRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .transactionType(type)
                .transactionId(transactionId)
                .description(description)
                .build();

        //accountServiceClient.updateBalance(accountId, request);
        // Call credit() or debit() based on type
        if ("CREDIT".equals(type)) {
            accountServiceClient.credit(accountId, request);
        } else if ("DEBIT".equals(type)) {
            accountServiceClient.debit(accountId, request);
        }
        log.debug(" {} ₹{} | Account: {}", type, amount, accountId);
    }

    private TransactionResponse finalizeTransaction(Transaction transaction, String username) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedDate(LocalDateTime.now());
        Transaction completedTransaction = transactionRepository.save(transaction);

        cacheIdempotencyKey(transaction.getIdempotencyKey(), transaction.getTransactionId());
        eventProducer.publishTransactionCompleted(completedTransaction);
        auditService.logTransactionCompleted(completedTransaction.getTransactionId(), username);

        log.info(" Transaction completed | ID: {} | Status: {}",
                completedTransaction.getTransactionId(), completedTransaction.getStatus());

        return TransactionMapper.toResponse(completedTransaction);
    }

    private void reverseTransfer(Transaction transaction) {
        // Credit back to source account (full amount including charges)
        updateAccountBalance(transaction.getFromAccountId(), transaction.getTotalAmount(),
                "CREDIT", transaction.getTransactionId() + "_REVERSAL",
                "Reversal of " + transaction.getTransactionId());

        // Debit from destination account (only transfer amount)
        updateAccountBalance(transaction.getToAccountId(), transaction.getAmount(),
                "DEBIT", transaction.getTransactionId() + "_REVERSAL",
                "Reversal of " + transaction.getTransactionId());
    }


    // VALIDATION METHODS


    private void validateTransferLimits(BigDecimal amount, TransferMode mode) {
        if (amount.compareTo(appProperties.getLimit().getPerTransaction()) > 0) {
            throw new TransactionLimitExceededException(
                    String.format("Amount exceeds per-transaction limit of ₹%s",
                            appProperties.getLimit().getPerTransaction()));
        }

        if (mode == TransferMode.RTGS &&
                amount.compareTo(appProperties.getTransfer().getRtgs().getMinAmount()) < 0) {
            throw new InvalidTransactionException(
                    String.format("RTGS requires minimum amount of ₹%s",
                            appProperties.getTransfer().getRtgs().getMinAmount()));
        }
    }

    private void validateAccountStatus(String status, String accountType) {
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new InvalidTransactionException(
                    String.format("%s account is not active. Current status: %s",
                            accountType, status));
        }
    }

    private void validateSufficientBalance(BigDecimal balance, BigDecimal required) {
        if (balance.compareTo(required) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: ₹%s, Required: ₹%s",
                            balance, required));
        }
    }

    private void validateDifferentAccounts(Long sourceAccountId, Long destinationAccountId) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new InvalidTransactionException(
                    "Cannot transfer money to the same account");
        }
    }

    private void validateReversalEligibility(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new InvalidTransactionException(
                    "Only successful transactions can be reversed. Current status: " +
                            transaction.getStatus());
        }

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new InvalidTransactionException(
                    "Transaction has already been reversed");
        }
    }


    // CALCULATION METHODS (Enhanced Switch Expressions)

    private BigDecimal calculateTransferCharges(BigDecimal amount, TransferMode mode) {
        return switch (mode) {
            case IMPS -> amount.compareTo(new BigDecimal("1000")) <= 0
                    ? new BigDecimal("5.00")
                    : new BigDecimal("15.00");

            case NEFT -> amount.compareTo(new BigDecimal("10000")) <= 0
                    ? new BigDecimal("2.50")
                    : new BigDecimal("5.00");

            case RTGS -> amount.compareTo(new BigDecimal("200000")) <= 0
                    ? new BigDecimal("25.00")
                    : new BigDecimal("50.00");
        };
    }

    private BigDecimal calculateGST(BigDecimal charges) {
        return charges.multiply(GST_RATE)
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
}