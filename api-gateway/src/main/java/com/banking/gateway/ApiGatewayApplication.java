package com.banking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * Central entry point for all banking microservices
 *
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);

        System.out.println("\n========================================");
        System.out.println("API Gateway Started Successfully!");
        System.out.println("========================================");
        System.out.println("Running on: http://localhost:8080");
        System.out.println("\nRoutes:");
        System.out.println("/user-service/** -> User Service (8081)");
        System.out.println("/account-service/** -> Account Service (8082)");
        System.out.println("/transaction-service/** -> Transaction Service (8083)");
        System.out.println("/loan-service/** -> Loan Service (8084)");
        System.out.println("\nReady to accept requests!");
        System.out.println("========================================\n");
    }
}
