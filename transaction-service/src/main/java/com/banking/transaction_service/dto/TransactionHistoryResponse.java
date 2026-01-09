package com.banking.transaction_service.dto;

import com.banking.transaction_service.enums.TransactionStatus;
import com.banking.transaction_service.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {
    private Long accountId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private List<TransactionSummary> transactions;
    private int totalTransactions;
    private int page;
    private int size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionSummary {
        private String transactionId;
        private String referenceNumber;
        private TransactionType type;
        private BigDecimal amount;
        private TransactionStatus status;
        private String description;
        private LocalDateTime date;
        private String counterPartyAccount; // Other account involved
    }
}