package com.banking.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginEvent {
    private Long userId;
    private String email;
    private String phone;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime loginTime;
    private String eventType;
}
