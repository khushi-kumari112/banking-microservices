package com.banking.loan_service.enums;

/**
 * EMI Status Enum
 * Status of individual EMI payments
 */
public enum EmiStatus {
    PENDING,    // Not yet due or unpaid
    PAID,       // Paid on time
    OVERDUE,    // Past due date
    PARTIAL,    // Partially paid
    WAIVED;     // Penalty/EMI waived

    public boolean isPaid() {
        return this == PAID || this == WAIVED;
    }

    public boolean needsPayment() {
        return this == PENDING || this == OVERDUE || this == PARTIAL;
    }
}