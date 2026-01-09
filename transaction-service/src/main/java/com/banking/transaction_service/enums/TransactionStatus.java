package com.banking.transaction_service.enums;

public enum TransactionStatus {
    PENDING,    // Transaction initiated
    SUCCESS,    // Transaction completed successfully
    FAILED,     // Transaction failed
    REVERSED    // Transaction reversed/cancelled
}