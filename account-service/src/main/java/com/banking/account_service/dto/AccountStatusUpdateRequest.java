package com.banking.account_service.dto;

import com.banking.account_service.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating account status
 *

 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusUpdateRequest {


    @NotBlank(message = "Account number is required")
    private String accountNumber;

    /**
     * New status: ACTIVE, INACTIVE, BLOCKED, CLOSED
     */
    @NotNull(message = "Status is required")
    private AccountStatus status;


    private String reason;
}
