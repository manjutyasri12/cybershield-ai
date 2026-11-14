package com.cybershield.ai.feature.agents.base;

import androidx.annotation.NonNull;

public class AgentResult {

    private final String agentId;
    private final float riskScore;
    private final boolean isThreatDetected;
    private final String threatType;
    private final String summary;
    private final long timestamp;

    public AgentResult(@NonNull String agentId, float riskScore, boolean isThreatDetected, @NonNull String threatType, @NonNull String summary, long timestamp) {
        this.agentId = agentId;
        this.riskScore = riskScore;
        this.isThreatDetected = isThreatDetected;
        this.threatType = threatType;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getAgentId() { return agentId; }
    public float getRiskScore() { return riskScore; }
    public boolean isThreatDetected() { return isThreatDetected; }
    @NonNull
    public String getThreatType() { return threatType; }
    @NonNull
    public String getSummary() { return summary; }
    public long getTimestamp() { return timestamp; }
}
