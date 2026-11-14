package com.cybershield.ai.feature.agents.payment_protection.model;

import androidx.annotation.NonNull;

public class PaymentProtectionResult {

    private final float fraudScore;
    private final float confidenceScore;
    private final String reason;
    private final PaymentAction recommendedAction;
    private final String targetVpa;
    private final double amount;
    private final boolean isNewBeneficiary;
    private final int failedAttemptsCount;
    private final long timestamp;

    public PaymentProtectionResult(
            float fraudScore,
            float confidenceScore,
            @NonNull String reason,
            @NonNull PaymentAction recommendedAction,
            @NonNull String targetVpa,
            double amount,
            boolean isNewBeneficiary,
            int failedAttemptsCount,
            long timestamp
    ) {
        this.fraudScore = fraudScore;
        this.confidenceScore = confidenceScore;
        this.reason = reason;
        this.recommendedAction = recommendedAction;
        this.targetVpa = targetVpa;
        this.amount = amount;
        this.isNewBeneficiary = isNewBeneficiary;
        this.failedAttemptsCount = failedAttemptsCount;
        this.timestamp = timestamp;
    }

    public float getFraudScore() { return fraudScore; }
    public float getConfidenceScore() { return confidenceScore; }
    @NonNull
    public String getReason() { return reason; }
    @NonNull
    public PaymentAction getRecommendedAction() { return recommendedAction; }
    @NonNull
    public String getTargetVpa() { return targetVpa; }
    public double getAmount() { return amount; }
    public boolean isNewBeneficiary() { return isNewBeneficiary; }
    public int getFailedAttemptsCount() { return failedAttemptsCount; }
    public long getTimestamp() { return timestamp; }
}
