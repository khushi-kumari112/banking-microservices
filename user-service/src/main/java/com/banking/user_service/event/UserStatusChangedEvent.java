package com.banking.user_service.event;

import com.banking.user_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusChangedEvent {
    private Long userId;
    private String email;
    private UserStatus oldStatus;
    private UserStatus newStatus;
    private String reason;
    private LocalDateTime timestamp;
    private String eventType;
}

