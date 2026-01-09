package com.banking.account_service.entity;

import com.banking.account_service.enums.AccountStatus;
import com.banking.account_service.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Entity
 * Represents a banking account (Savings, Current, Salary, Wallet)
 *
 * Real Banking Standards:
 * - Account Number: 14 digits (matches SBI, HDFC format)
 * - IFSC Code: 11 characters (standard format)
 * - Branch Code: Identifies bank branch
 *
 * @author Khushi Kumari
 * @version 1.0
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number", unique = true),
        @Index(name = "idx_internal_user_id", columnList = "internal_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", unique = true, nullable = false, length = 14)
    private String accountNumber; // e.g., "12345678901234"

    @Column(name = "internal_user_id", nullable = false, length = 50)
    private String internalUserId; // Links to User Service

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    private String ifscCode; // e.g., "SBIN0001234"

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode; // e.g., "BR001"

    @Column(name = "branch_name", length = 100)
    private String branchName; // e.g., "Patna Main Branch"

    @Column(name = "currency", length = 3)
    private String currency = "INR";

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();


    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "closure_date")
    private LocalDateTime closureDate;

    @Column(name = "closure_reason", length = 500)
    private String closureReason;


    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    // Helper methods for balance operations
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
