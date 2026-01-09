package com.banking.user_service.exception.custom;

public class OnboardingIncompleteException extends RuntimeException {
    public OnboardingIncompleteException(String currentStep, String requiredStep) {
        super(String.format("User onboarding incomplete. Current: %s, Required: %s", currentStep, requiredStep));
    }
}
