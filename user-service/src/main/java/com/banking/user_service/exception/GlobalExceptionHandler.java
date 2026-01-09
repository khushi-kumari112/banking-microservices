package com.banking.user_service.exception;

import com.banking.user_service.exception.custom.*;
import com.banking.user_service.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for User Service
 *
 * Centralized exception handling for all controllers and services.
 * Provides consistent error response format and appropriate HTTP status codes.
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //  DOMAIN-SPECIFIC EXCEPTIONS

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request) {

        log.error(" UserNotFoundException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, HttpServletRequest request) {

        log.error("⚠ UserAlreadyExistsException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidMPINException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidMPINException(
            InvalidMPINException ex, HttpServletRequest request) {

        log.error(" InvalidMPINException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MPINMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMPINMismatchException(
            MPINMismatchException ex, HttpServletRequest request) {

        log.error(" MPINMismatchException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountBlockedException(
            AccountBlockedException ex, HttpServletRequest request) {

        log.error(" AccountBlockedException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidInputException(
            InvalidInputException ex, HttpServletRequest request) {

        log.error(" InvalidInputException at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleOtpExpiredException(
            OtpExpiredException ex, HttpServletRequest request) {

        log.error(" OtpExpiredException at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailNotVerified(
            EmailNotVerifiedException ex, HttpServletRequest request) {

        log.error(" EmailNotVerifiedException at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidEmailDomain(
            InvalidEmailDomainException ex, HttpServletRequest request) {

        log.error(" InvalidEmailDomainException at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OnboardingIncompleteException.class)
    public ResponseEntity<ApiResponse<Object>> handleOnboardingIncomplete(
            OnboardingIncompleteException ex, HttpServletRequest request) {

        log.error(" OnboardingIncompleteException at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    //  SPRING FRAMEWORK EXCEPTIONS

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.error(" Validation Error at {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message("Validation failed for one or more fields")
                .data(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.error(" Missing Parameter at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingHeader(
            MissingRequestHeaderException ex, HttpServletRequest request) {

        log.error(" Missing Header at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format("Required header '%s' is missing", ex.getHeaderName());

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.error(" Type Mismatch at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.error(" Malformed JSON at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Malformed JSON request. Please check your request body format.";

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.error(" IllegalArgumentException at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex.getMessage();

        if (message != null && message.contains("UserRole")) {
            message = "Invalid role. Allowed values: CUSTOMER, ADMIN, SUPPORT";
        } else if (message != null && message.contains("UserStatus")) {
            message = "Invalid status. Allowed values: ACTIVE, BLOCKED, SUSPENDED";
        } else if (message != null && message.contains("KycStatus")) {
            message = "Invalid KYC status. Allowed values: PENDING, VERIFIED, REJECTED";
        } else if (message != null && message.contains("No enum constant")) {
            message = "Invalid enum value provided. Please check allowed values.";
        }

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.error(" Method Not Allowed at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                ex.getSupportedHttpMethods()
        );

        return buildErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.error(" Endpoint Not Found: {}", request.getRequestURI());

        String message = String.format(
                "Endpoint '%s' not found. Please check the URL and try again.",
                ex.getRequestURL()
        );

        return buildErrorResponse(message, HttpStatus.NOT_FOUND);
    }

    // SECURITY EXCEPTIONS

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.error(" Authentication Failed at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                "Authentication failed. Please provide valid credentials.",
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.error(" Bad Credentials at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                "Invalid credentials provided. Please check your email/phone and MPIN.",
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.error(" Access Denied at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorResponse(
                "You do not have permission to access this resource.",
                HttpStatus.FORBIDDEN
        );
    }

    // GENERIC EXCEPTION HANDLER

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error(" Unexpected Error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message("An unexpected error occurred. Please contact support if the issue persists.")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // HELPER METHOD

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(String message, HttpStatus status) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message(message)
                .data(null)
                .build();

        return new ResponseEntity<>(response, status);
    }
}
