package com.banking.user_service.config;

import com.banking.user_service.security.JwtAuthenticationEntryPoint;
import com.banking.user_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Banking Microservice
 *
 * Real-world banking security features:
 * - JWT-based stateless authentication
 * - CORS protection with whitelisted origins
 * - Role-based access control (RBAC)
 * - Public endpoints for registration/login
 * - Protected endpoints for user operations
 * - Admin-only endpoints with ADMIN role
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @Secured annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS (No authentication required) ==========
                        .requestMatchers(
                                "/api/v1/users/register/**",      // All registration steps
                                "/api/v1/users/authenticate",      // Login endpoint
                                "/api/v1/users/mpin/reset/**",     // MPIN reset flow
                                "/api/v1/admin/register",
                                "/actuator/health",                // Health check only
                                "/actuator/info"                   // Info endpoint
                        ).permitAll()

                        //  ADMIN ENDPOINTS (Requires ADMIN role)
                        .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")

                        //  PROTECTED ENDPOINTS (Requires authentication)
                        .requestMatchers(
                                "/api/v1/users/profile/**",
                                "/api/v1/users/address/**",
                                "/api/v1/users/kyc/**"
                        ).authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Configure exception handling for authentication failures
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Configure stateless session management (no server-side sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration
     *
     * Real-world banking approach:
     * - Whitelist specific frontend origins (not "*" in production)
     * - Allow credentials for cookie-based requests
     * - Specify allowed HTTP methods
     * - Control exposed headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins (UPDATE THESE FOR PRODUCTION)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",      // React frontend
                "http://localhost:4200",      // Angular frontend
                "http://localhost:8080",      // Another service
                "https://yourdomain.com"      // Production frontend
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "internal-user-id",
                "identifier",
                "email",
                "phone",
                "otp"
        ));

        // Exposed headers (accessible to frontend)
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password Encoder Bean
     * Uses BCrypt hashing algorithm (industry standard)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength factor 12 (recommended for banking)
    }

    /**
     * Authentication Manager Bean
     * Required for programmatic authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
