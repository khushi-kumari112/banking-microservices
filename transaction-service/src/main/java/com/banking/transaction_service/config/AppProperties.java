package com.banking.transaction_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "transaction")
@Data
public class AppProperties {
    private Limit limit;
    private Transfer transfer;
    private Long idempotencyTtl;

    @Data
    public static class Limit {
        private BigDecimal daily;
        private BigDecimal perTransaction;
        private BigDecimal largeAmountThreshold;
    }

    @Data
    public static class Transfer {
        private Imps imps;
        private Neft neft;
        private Rtgs rtgs;

        @Data
        public static class Imps {
            private boolean enabled;
        }

        @Data
        public static class Neft {
            private boolean enabled;
        }

        @Data
        public static class Rtgs {
            private boolean enabled;
            private BigDecimal minAmount;
        }
    }
}