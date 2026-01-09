package com.banking.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KYCSubmissionRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;  // AADHAAR, PAN, PASSPORT, DRIVING_LICENSE

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Document path is required")
    private String documentPath;  // Cloud storage path or base64

    private String additionalInfo;  // Optional notes
}

