package com.banking.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiCalculationResponse {
    private BigDecimal loanAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalAmount;
    private BigDecimal monthlyInterestRate;
}