package com.banking.loan_service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String internalUserId;
    private String username;
    private String email;
    private String fullName;
    private Integer creditScore;
}