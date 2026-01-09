package com.banking.loan_service.dto;

import com.banking.loan_service.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEligibilityRequest {
    private String internalUserId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Loan amount is required")
    private BigDecimal loanAmount;

    @NotNull(message = "Monthly income is required")
    private BigDecimal monthlyIncome;

    private BigDecimal existingEmi = BigDecimal.ZERO;

    @NotNull(message = "Tenure is required")
    private Integer tenureMonths;

    private Integer creditScore;
}
