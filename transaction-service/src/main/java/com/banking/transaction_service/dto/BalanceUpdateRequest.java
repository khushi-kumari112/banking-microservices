package com.banking.transaction_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateRequest {
    private Long accountId;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType; // CREDIT or DEBIT
    private String transactionId;
    private String description;
}