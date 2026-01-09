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
public class AccountBalanceResponse {
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String accountStatus;
    private String currency;
}
