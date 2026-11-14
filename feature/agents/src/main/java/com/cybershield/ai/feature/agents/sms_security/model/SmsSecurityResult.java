package com.cybershield.ai.feature.agents.sms_security.model;

import androidx.annotation.NonNull;

public class SmsSecurityResult {

    private final float threatScore;
    private final float confidenceScore;
    private final SmsCategory category;
    private final String reason;
    private final String senderAddress;
    private final String bodySnippet;
    private final long timestamp;

    public SmsSecurityResult(
            float threatScore,
            float confidenceScore,
            @NonNull SmsCategory category,
            @NonNull String reason,
            @NonNull String senderAddress,
            @NonNull String bodySnippet,
            long timestamp
    ) {
        this.threatScore = threatScore;
        this.confidenceScore = confidenceScore;
        this.category = category;
        this.reason = reason;
        this.senderAddress = senderAddress;
        this.bodySnippet = bodySnippet;
        this.timestamp = timestamp;
    }

    public float getThreatScore() { return threatScore; }
    public float getConfidenceScore() { return confidenceScore; }
    @NonNull
    public SmsCategory getCategory() { return category; }
    @NonNull
    public String getReason() { return reason; }
    @NonNull
    public String getSenderAddress() { return senderAddress; }
    @NonNull
    public String getBodySnippet() { return bodySnippet; }
    public long getTimestamp() { return timestamp; }
}
