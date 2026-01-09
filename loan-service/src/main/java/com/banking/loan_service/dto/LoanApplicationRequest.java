package com.banking.loan_service.dto;

import com.banking.loan_service.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Loan Application Request DTO
 * Used when a user applies for a new loan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    // Set from header - internal-user-id
    private String internalUserId;

    @NotBlank(message = "Account number is required")
    @Size(min = 14, max = 20, message = "Account number must be between 14 and 20 digits")
    private String accountNumber;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000", message = "Minimum loan amount is ₹10,000")
    @DecimalMax(value = "10000000", message = "Maximum loan amount is ₹1,00,00,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "5.0", message = "Minimum interest rate is 5%")
    @DecimalMax(value = "18.0", message = "Maximum interest rate is 18%")
    private Double interestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 360, message = "Maximum tenure is 360 months (30 years)")
    private Integer tenureMonths;

    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 200, message = "Purpose must be between 10 and 200 characters")
    private String purpose;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "10000", message = "Minimum monthly income is ₹10,000")
    private BigDecimal monthlyIncome;

    private BigDecimal existingEmi = BigDecimal.ZERO;

    @NotBlank(message = "Employment type is required")
    @Pattern(regexp = "SALARIED|SELF_EMPLOYED", message = "Employment type must be SALARIED or SELF_EMPLOYED")
    private String employmentType;

    private String employerName;

    private Integer creditScore; // Optional - will be fetched if not provided
}