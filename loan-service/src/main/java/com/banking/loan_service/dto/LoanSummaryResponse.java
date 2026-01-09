package com.banking.loan_service.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSummaryResponse {
    private Long totalLoans;
    private Long activeLoans;
    private Long closedLoans;
    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalDisbursedAmount;
    private BigDecimal monthlyEmiAmount;
    private Integer overdueEmisCount;
    private BigDecimal totalPenaltyAmount;
}