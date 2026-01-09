package com.banking.loan_service.entity;

import com.banking.loan_service.enums.EmiStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EMI Schedule Entity
 * Tracks monthly installment payments
 */
@Entity
@Table(name = "emi_schedules", indexes = {
        @Index(name = "idx_loan_id", columnList = "loanId"),
        @Index(name = "idx_loan_number", columnList = "loanNumber"),
        @Index(name = "idx_emi_number", columnList = "emiNumber"),
        @Index(name = "idx_due_date", columnList = "dueDate"),
        @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false, length = 20)
    private String loanNumber;

    @Column(nullable = false)
    private Integer emiNumber; // 1, 2, 3... up to tenure

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interestAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmiStatus status = EmiStatus.PENDING;

    // Payment Details
    @Column
    private LocalDate paidDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(length = 50)
    private String paymentTransactionId;

    // Late Payment
    @Column
    private Integer daysOverdue = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO; // EMI + Penalty

    // Balance After Payment
    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Helper Methods
    public boolean isPending() {
        return this.status == EmiStatus.PENDING;
    }

    public boolean isPaid() {
        return this.status == EmiStatus.PAID;
    }

    public boolean isOverdue() {
        return this.status == EmiStatus.OVERDUE ||
                (this.status == EmiStatus.PENDING && LocalDate.now().isAfter(this.dueDate));
    }

    public void markAsPaid(BigDecimal amount, String transactionId) {
        this.status = EmiStatus.PAID;
        this.paidDate = LocalDate.now();
        this.paidAmount = amount;
        this.totalPaid = amount.add(this.penaltyAmount);
        this.paymentTransactionId = transactionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsOverdue() {
        if (this.status == EmiStatus.PENDING && LocalDate.now().isAfter(this.dueDate)) {
            this.status = EmiStatus.OVERDUE;
            this.daysOverdue = (int) java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void applyPenalty(BigDecimal penalty) {
        this.penaltyAmount = this.penaltyAmount.add(penalty);
        this.updatedAt = LocalDateTime.now();
    }
}