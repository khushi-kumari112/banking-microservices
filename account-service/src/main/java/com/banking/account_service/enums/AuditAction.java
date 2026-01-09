package com.banking.account_service.enums;

/**
 * Audit Actions for Account Operations
 */
public enum AuditAction {
    CREATE_ACCOUNT,
    VIEW_ACCOUNT,
    UPDATE_BALANCE,
    UPDATE_STATUS,
    CLOSE_ACCOUNT,
    CHECK_BALANCE,
    TRANSFER_INITIATED,
    TRANSFER_COMPLETED,
    TRANSFER_FAILED
}