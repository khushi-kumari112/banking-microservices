package com.banking.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpinResetRequest {

    @NotBlank(message = "New MPIN is required")
    @Pattern(regexp = "^\\d{4}$|^\\d{6}$", message = "MPIN must be 4 or 6 digits")
    private String newMpin;

    @NotBlank(message = "Confirm new MPIN is required")
    private String confirmNewMpin;
}