package com.banking.account_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for balance updates
 * Used by Transaction Service to update account balances
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateRequest {

    /**
     * Account number to update

     */
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    /**
     * Amount to credit/debit
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    /**
     * Operation type: CREDIT or DEBIT
     */
    @NotBlank(message = "Operation is required")
    private String operation;  // "CREDIT" or "DEBIT"

    /**
     * Transaction ID from Transaction Service
     */
    private String transactionId;

    /**
     * Optional remarks
     */
    private String remarks;
}
