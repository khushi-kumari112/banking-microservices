package com.banking.loan_service.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String description;
}