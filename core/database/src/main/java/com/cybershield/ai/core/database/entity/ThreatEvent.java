package com.cybershield.ai.core.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for security threats detected by AI Agents.
 */
@Entity(tableName = "threat_events")
public class ThreatEvent {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String agentName;
    private String threatType; // PHISHING, MALWARE, NETWORK_MITM
    private float riskScore; // 0.0 to 1.0
    private String summary;
    private String encryptedDetails;
    private long timestamp;
    private boolean isMitigated;

    public ThreatEvent(String agentName, String threatType, float riskScore, String summary, String encryptedDetails, long timestamp, boolean isMitigated) {
        this.agentName = agentName;
        this.threatType = threatType;
        this.riskScore = riskScore;
        this.summary = summary;
        this.encryptedDetails = encryptedDetails;
        this.timestamp = timestamp;
        this.isMitigated = isMitigated;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }

    public float getRiskScore() { return riskScore; }
    public void setRiskScore(float riskScore) { this.riskScore = riskScore; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEncryptedDetails() { return encryptedDetails; }
    public void setEncryptedDetails(String encryptedDetails) { this.encryptedDetails = encryptedDetails; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isMitigated() { return isMitigated; }
    public void setMitigated(boolean mitigated) { isMitigated = mitigated; }
}
