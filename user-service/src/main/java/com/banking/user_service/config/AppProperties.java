package com.banking.user_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Mpin mpin = new Mpin();
    private Otp otp = new Otp();

    @Data
    public static class Mpin {
        private int minLength = 4;
        private int maxLength = 6;
        private int maxLoginAttempts = 3;
        private int lockoutDurationMinutes = 30;
    }

    @Data
    public static class Otp {
        private int expiryMinutes = 5;
    }
}
