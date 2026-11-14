package com.cybershield.ai.feature.agents.call_intelligence.model;

import androidx.annotation.NonNull;

public class CallIntelligenceResult {

    private final float riskScore;
    private final float confidenceScore;
    private final String explanation;
    private final CallRecommendation recommendation;
    private final String phoneNumber;
    private final boolean isUnknownNumber;
    private final int callFrequencyLastHour;
    private final long timestamp;

    public CallIntelligenceResult(
            float riskScore,
            float confidenceScore,
            @NonNull String explanation,
            @NonNull CallRecommendation recommendation,
            @NonNull String phoneNumber,
            boolean isUnknownNumber,
            int callFrequencyLastHour,
            long timestamp
    ) {
        this.riskScore = riskScore;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
        this.recommendation = recommendation;
        this.phoneNumber = phoneNumber;
        this.isUnknownNumber = isUnknownNumber;
        this.callFrequencyLastHour = callFrequencyLastHour;
        this.timestamp = timestamp;
    }

    public float getRiskScore() { return riskScore; }
    public float getConfidenceScore() { return confidenceScore; }
    @NonNull
    public String getExplanation() { return explanation; }
    @NonNull
    public CallRecommendation getRecommendation() { return recommendation; }
    @NonNull
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isUnknownNumber() { return isUnknownNumber; }
    public int getCallFrequencyLastHour() { return callFrequencyLastHour; }
    public long getTimestamp() { return timestamp; }
}
