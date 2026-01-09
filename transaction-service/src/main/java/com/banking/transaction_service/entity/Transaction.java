package com.banking.transaction_service.entity;

import com.banking.transaction_service.enums.TransactionStatus;
import com.banking.transaction_service.enums.TransactionType;
import com.banking.transaction_service.enums.TransferMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_from_account", columnList = "fromAccountId"),
        @Index(name = "idx_to_account", columnList = "toAccountId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_date", columnList = "createdDate"),
        @Index(name = "idx_reference_number", columnList = "referenceNumber")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(unique = true, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransferMode transferMode;


    @Column(name = "from_account_id", nullable = true)
    private Long fromAccountId;
    @Column(name = "from_account_number", nullable = true)
    private String fromAccountNumber;

    @Column
    private Long toAccountId;

    @Column(length = 20)
    private String toAccountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal chargesAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String remarks;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 100)
    private String idempotencyKey;


    @Column(nullable = false, length = 100)
    private String initiatedBy;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column
    private LocalDateTime completedDate;

    @Column
    private LocalDateTime reversedDate;

    @Column(length = 500)
    private String reversalReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
        if (totalAmount == null) {
            totalAmount = amount.add(chargesAmount).add(taxAmount);
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}