package com.banking.loan_service.dto;

import com.banking.loan_service.enums.LoanStatus;
import com.banking.loan_service.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private String loanNumber;
    private String internalUserId;
    private String accountNumber;
    private LoanType loanType;
    private BigDecimal loanAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private Integer paidEmis;
    private Integer remainingEmis;
    private LoanStatus status;
    private String purpose;
    private BigDecimal monthlyIncome;
    private Integer creditScore;
    private String creditRating;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursementDate;
    private LocalDate nextEmiDueDate;
    private LocalDate firstEmiDate;
    private LocalDate lastEmiDate;
    private Boolean isDisbursed;
    private Integer missedEmis;
    private BigDecimal penaltyAmount;
    private LocalDate lastPaymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}