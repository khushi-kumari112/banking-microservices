package com.banking.loan_service.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEligibilityResponse {
    private boolean eligible;
    private String reason;
    private Integer creditScore;
    private String creditRating;
    private BigDecimal maxEligibleAmount;
    private BigDecimal proposedEmi;
    private BigDecimal totalMonthlyEmi;
    private Double incomeRatio;
    private Integer activeLoansCount;
}
