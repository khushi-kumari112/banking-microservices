package com.banking.account_service.entity;

import com.banking.account_service.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Account Audit Entity
 * Tracks all account operations for compliance & auditing
 *
 * Real Banking Requirement:
 * - RBI mandate: Keep audit trail for 10 years
 * - Track who did what, when, and from where
 * - Used for fraud detection & compliance
 */
@Entity
@Table(name = "account_audit", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number"),
        @Index(name = "idx_internal_user_id", columnList = "internal_user_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "account_number", length = 14)
    private String accountNumber;

    @Column(name = "internal_user_id", length = 50)
    private String internalUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "performed_by", length = 50)
    private String performedBy; // Internal user ID or SYSTEM

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "transaction_id", length = 100)
    private String transactionId; // Reference to transaction
}

