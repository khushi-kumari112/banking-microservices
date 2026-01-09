package com.banking.loan_service.dto;

import com.banking.loan_service.enums.EmiStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiScheduleResponse {
    private Long id;
    private String loanNumber;
    private Integer emiNumber;
    private BigDecimal emiAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private LocalDate dueDate;
    private EmiStatus status;
    private LocalDate paidDate;
    private BigDecimal paidAmount;
    private BigDecimal penaltyAmount;
    private Integer daysOverdue;
    private BigDecimal outstandingBalance;
}
