package com.banking.loan_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private LocalDateTime timestamp;
}
