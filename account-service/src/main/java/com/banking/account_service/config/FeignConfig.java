// ═══════════════════════════════════════════════════════════════════════
// File: FeignConfig.java
// ═══════════════════════════════════════════════════════════════════════
package com.banking.account_service.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign Client Configuration
 * Configures timeout, retry logic for User Service communication
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Log all request/response details
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(
                5000, TimeUnit.MILLISECONDS,  // Connect timeout
                5000, TimeUnit.MILLISECONDS,  // Read timeout
                true                          // Follow redirects
        );
    }

    @Bean
    public Retryer retryer() {
        // Retry failed requests: period=100ms, max period=1s, max attempts=3
        return new Retryer.Default(100, 1000, 3);
    }
}