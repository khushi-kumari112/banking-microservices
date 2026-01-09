package com.banking.transaction_service.enums;

public enum AuditAction {
    TRANSACTION_INITIATED,
    TRANSACTION_COMPLETED,
    TRANSACTION_FAILED,
    TRANSACTION_REVERSED,
    BALANCE_UPDATED,
    LIMIT_EXCEEDED
}