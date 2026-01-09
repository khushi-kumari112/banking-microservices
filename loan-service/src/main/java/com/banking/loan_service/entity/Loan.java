package com.banking.loan_service.entity;

import com.banking.loan_service.enums.LoanStatus;
import com.banking.loan_service.enums.LoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Loan Entity
 * Represents a loan in the banking system
 */
@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loan_number", columnList = "loanNumber", unique = true),
        @Index(name = "idx_internal_user_id", columnList = "internalUserId"),
        @Index(name = "idx_account_number", columnList = "accountNumber"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_loan_type", columnList = "loanType")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String loanNumber; // Format: LN + 16 digits

    @Column(nullable = false, length = 50)
    private String internalUserId; // From User Service

    @Column(nullable = false, length = 20)
    private String accountNumber; // Account for disbursement

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanType loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount; // Principal amount

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate; // Annual interest rate (%)

    @Column(nullable = false)
    private Integer tenureMonths; // Loan duration in months

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount; // Monthly EMI

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount; // Principal + Interest

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount; // Outstanding amount

    @Column(nullable = false)
    private Integer paidEmis = 0; // Number of EMIs paid

    @Column(nullable = false)
    private Integer remainingEmis; // Remaining EMIs

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status = LoanStatus.PENDING;

    // Application Details
    @Column(nullable = false, length = 200)
    private String purpose; // Loan purpose

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(precision = 10, scale = 2)
    private BigDecimal existingEmi = BigDecimal.ZERO;

    @Column(length = 50)
    private String employmentType; // SALARIED, SELF_EMPLOYED

    @Column(length = 100)
    private String employerName;

    // Credit Assessment
    @Column
    private Integer creditScore; // 300-900

    @Column(length = 20)
    private String creditRating; // POOR, FAIR, GOOD, EXCELLENT

    // Important Dates
    @Column
    private LocalDate applicationDate;

    @Column
    private LocalDate approvalDate;

    @Column
    private LocalDate disbursementDate;

    @Column
    private LocalDate firstEmiDate;

    @Column
    private LocalDate lastEmiDate;

    @Column
    private LocalDate nextEmiDueDate;

    @Column
    private LocalDate closureDate;

    // Approval Details
    @Column(length = 50)
    private String approvedBy; // Admin username

    @Column(columnDefinition = "TEXT")
    private String approvalRemarks;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Disbursement
    @Column
    private Boolean isDisbursed = false;

    @Column(length = 50)
    private String disbursementTransactionId;

    // Late Payment Tracking
    @Column
    private Integer missedEmis = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column
    private LocalDate lastPaymentDate;

    // Audit Fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String createdBy; // Username

    @Column(length = 50)
    private String updatedBy; // Username

    // Soft Delete
    @Column
    private Boolean isActive = true;

    // Helper Methods
    public boolean isApproved() {
        return this.status == LoanStatus.APPROVED || this.status == LoanStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == LoanStatus.ACTIVE;
    }

    public boolean isClosed() {
        return this.status == LoanStatus.CLOSED ||
                this.status == LoanStatus.FORECLOSED;
    }

    public boolean canRepay() {
        return this.status == LoanStatus.ACTIVE && this.remainingAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public void makePayment(BigDecimal amount) {
        this.remainingAmount = this.remainingAmount.subtract(amount);
        this.paidEmis++;
        this.remainingEmis--;
        this.lastPaymentDate = LocalDate.now();

        if (this.remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = LoanStatus.CLOSED;
            this.closureDate = LocalDate.now();
        }
    }

    public void applyPenalty(BigDecimal penalty) {
        this.penaltyAmount = this.penaltyAmount.add(penalty);
        this.remainingAmount = this.remainingAmount.add(penalty);
        this.missedEmis++;
    }

    @PrePersist
    public void prePersist() {
        if (this.applicationDate == null) {
            this.applicationDate = LocalDate.now();
        }
        if (this.remainingAmount == null) {
            this.remainingAmount = this.totalAmount;
        }
        if (this.remainingEmis == null) {
            this.remainingEmis = this.tenureMonths;
        }
    }
}