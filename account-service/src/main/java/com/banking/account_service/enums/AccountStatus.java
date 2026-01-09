package com.banking.account_service.enums;

/**
 * Account Status - Lifecycle Management
 *
 * ACTIVE: Normal operations allowed
 * INACTIVE: Temporarily disabled (can be reactivated)
 * BLOCKED: Frozen by bank (fraud, legal issues)
 * DORMANT: No transactions for 2+ years
 * CLOSED: Permanently closed
 */
public enum AccountStatus {
    ACTIVE,      // Full access
    INACTIVE,    // Temporarily disabled
    BLOCKED,     // Frozen (fraud/legal)
    DORMANT,     // No activity for 2+ years
    CLOSED       // Permanently closed
}