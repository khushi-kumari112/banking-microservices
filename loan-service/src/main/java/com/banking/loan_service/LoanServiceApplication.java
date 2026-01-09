package com.banking.loan_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Loan Service Application
 *
 * Banking Loan Management Microservice
 * Features:
 * - Loan applications and approvals
 * - EMI calculations and tracking
 * - Loan repayment processing
 * - Credit score checks
 * - Kafka event publishing
 * - Redis caching
 * - Integration with Account Service
 *
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableTransactionManagement
public class LoanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
        System.out.println("""
                
                ============================================
                    LOAN SERVICE STARTED SUCCESSFULLY    
                ============================================
                    Port: 8084
                    Database: loan_db
                    Features:
                    ✓ Loan Applications
                    ✓ EMI Calculator
                    ✓ Repayment Tracking
                    ✓ Kafka Events
                    ✓ Redis Caching
                    ✓ Account Integration
                ============================================
                """);
    }
}