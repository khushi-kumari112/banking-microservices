package com.banking.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Transaction Service - Banking Microservice
 *
 * Features:
 * - Money Transfer (IMPS/NEFT/RTGS)
 * - Deposit/Withdrawal
 * - Redis Caching
 * - Kafka Event Publishing
 * - JWT Security
 * - Feign Client for Account Service
 *
 * Port: 8083
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableKafka
@EnableAsync
@EnableJpaAuditing
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════╗
            ║      Transaction Service Started Successfully!           ║
            ║      Port: 8083                                          ║
            ║      Endpoints: /api/v1/transactions/**                  ║
            ╚══════════════════════════════════════════════════════════╝
            """);
    }
}