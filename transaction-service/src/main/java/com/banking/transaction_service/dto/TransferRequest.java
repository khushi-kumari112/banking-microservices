package com.banking.transaction_service.dto;

import com.banking.transaction_service.enums.TransferMode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    @NotNull(message = "To account number is required")
    @Pattern(regexp = "\\d{10,20}", message = "Invalid account number format")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @DecimalMax(value = "50000.00", message = "Amount cannot exceed 50,000.00 per transaction")
    private BigDecimal amount;

    @NotNull(message = "Transfer mode is required")
    private TransferMode transferMode;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
    private String idempotencyKey; // For preventing duplicate transactions
}
