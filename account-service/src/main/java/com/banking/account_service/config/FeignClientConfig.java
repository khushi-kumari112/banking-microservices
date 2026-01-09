package com.banking.account_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                try {
                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();

                        String authHeader = request.getHeader("Authorization");
                        if (authHeader != null && !authHeader.isEmpty()) {
                            template.header("Authorization", authHeader);
                            log.info(" Authorization header forwarded to User Service");
                        } else {
                            log.warn("No Authorization header found in request");
                        }
                    }
                } catch (Exception e) {
                    log.error(" Error forwarding headers", e);
                }
            }
        };
    }
}