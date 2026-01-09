package com.banking.loan_service.enums;

/**
 * Loan Status Enum
 * Lifecycle stages of a loan application
 */
public enum LoanStatus {
    PENDING,        // Application submitted, awaiting review
    UNDER_REVIEW,   // Being reviewed by loan officer
    APPROVED,       // Approved, awaiting disbursement
    REJECTED,       // Application rejected
    ACTIVE,         // Loan disbursed and active
    OVERDUE,        // One or more EMIs missed
    DEFAULTED,      // Multiple EMIs missed (serious)
    CLOSED,         // Fully repaid
    FORECLOSED;     // Closed before tenure completion

    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE;
    }

    public boolean isClosed() {
        return this == CLOSED || this == FORECLOSED;
    }

    public boolean canDisburse() {
        return this == APPROVED;
    }

    public boolean canRepay() {
        return this == ACTIVE || this == OVERDUE;
    }
}