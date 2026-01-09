package com.banking.loan_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest {
    private String approvedBy;

    @NotBlank(message = "Remarks are required")
    private String remarks;
}
