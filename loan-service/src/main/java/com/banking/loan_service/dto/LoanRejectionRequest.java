package com.banking.loan_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRejectionRequest {
    private String rejectedBy;

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
