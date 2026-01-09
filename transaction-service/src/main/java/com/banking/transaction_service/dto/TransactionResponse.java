package com.banking.transaction_service.dto;

import com.banking.transaction_service.enums.TransactionStatus;
import com.banking.transaction_service.enums.TransactionType;
import com.banking.transaction_service.enums.TransferMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {
    private Long id;
    private String transactionId;
    private String referenceNumber;
    private TransactionType transactionType;
    private TransferMode transferMode;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal chargesAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private TransactionStatus status;
    private String description;
    private String remarks;
    private String failureReason;
    private String initiatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
}
