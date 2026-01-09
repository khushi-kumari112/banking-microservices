package com.banking.account_service.enums;

/**
 * Transaction Types for Auditing
 * Used by AccountAudit to track all account operations
 */
public enum TransactionType {
    ACCOUNT_CREATED,
    BALANCE_CREDITED,
    BALANCE_DEBITED,
    STATUS_CHANGED,
    ACCOUNT_CLOSED,
    KYC_UPDATED,
    LIMIT_CHANGED
}
