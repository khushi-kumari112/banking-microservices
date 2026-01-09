package com.banking.transaction_service.util;

import com.banking.transaction_service.dto.TransactionResponse;
import com.banking.transaction_service.entity.Transaction;

public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .transactionType(transaction.getTransactionType())
                .transferMode(transaction.getTransferMode())
                .fromAccountId(transaction.getFromAccountId())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountId(transaction.getToAccountId())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .chargesAmount(transaction.getChargesAmount())
                .taxAmount(transaction.getTaxAmount())
                .totalAmount(transaction.getTotalAmount())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .remarks(transaction.getRemarks())
                .failureReason(transaction.getFailureReason())
                .initiatedBy(transaction.getInitiatedBy())  // ✅ Now matches (String)
                .createdDate(transaction.getCreatedDate())
                .completedDate(transaction.getCompletedDate())
                .build();
    }
}