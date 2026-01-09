package com.banking.loan_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiPaymentResponse {
    private String loanNumber;
    private Integer emiNumber;
    private BigDecimal paidAmount;
    private BigDecimal emiAmount;
    private BigDecimal penaltyAmount;
    private String transactionId;
    private LocalDate paidDate;
    private BigDecimal remainingAmount;
    private Integer remainingEmis;
    private LocalDate nextEmiDueDate;
}
