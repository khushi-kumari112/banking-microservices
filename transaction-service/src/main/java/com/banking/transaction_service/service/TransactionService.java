package com.banking.transaction_service.service;

import com.banking.transaction_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse getTransaction(String transactionId);
    TransactionResponse getTransactionByReference(String referenceNumber);
    Page<TransactionResponse> getTransactionHistory(Long accountId, Pageable pageable);


    TransactionResponse transfer(TransferRequest request, Long userId, String username);
    TransactionResponse deposit(DepositRequest request, Long userId, String username);
    TransactionResponse withdrawal(WithdrawalRequest request, Long userId, String username);
    TransactionResponse reverseTransaction(String transactionId, String reason, String username);
}