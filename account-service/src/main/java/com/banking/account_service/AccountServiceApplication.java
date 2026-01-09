package com.banking.account_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Account Service Application
 *
 * Banking Account Management Microservice
 * Handles: Account creation, balance management, status updates
 *
 * Features:
 * ✅ Feign Client for User Service communication
 * ✅ Redis caching for performance
 * ✅ Kafka for event publishing
 * ✅ JPA for database operations
 * ✅ Transaction management
 *
 * @author Khushi Kumari
 * @version 2.0
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.banking.account_service.feign")
@EnableCaching
@EnableKafka
@EnableJpaRepositories
@EnableTransactionManagement
@EnableConfigurationProperties
@Slf4j
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);

        log.info("========================================");
        log.info(" Account Service Started Successfully!");
        log.info("========================================");
        log.info("Running on: http://localhost:8082");
        log.info("Features:");
        log.info("   Account Creation with KYC Verification");
        log.info("   Balance Management");
        log.info("   Real-world Banking Validations");
        log.info("   Redis Caching Enabled");
        log.info("   Audit Trail Logging");
        log.info("   Circuit Breaker for User Service");
        log.info("========================================");
        log.info("Endpoints: /api/v1/account/**");
        log.info("Ready to accept requests!");
        log.info("========================================");
    }
}