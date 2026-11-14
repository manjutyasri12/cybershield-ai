package com.cybershield.ai.core.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Threat consensus response returned by Fog Computing Nodes.
 */
public class FogResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("risk_score")
    private float riskScore;

    @SerializedName("consensus_verdict")
    private String consensusVerdict;

    @SerializedName("mitigation_action")
    private String mitigationAction;

    public FogResponse(String status, float riskScore, String consensusVerdict, String mitigationAction) {
        this.status = status;
        this.riskScore = riskScore;
        this.consensusVerdict = consensusVerdict;
        this.mitigationAction = mitigationAction;
    }

    public String getStatus() { return status; }
    public float getRiskScore() { return riskScore; }
    public String getConsensusVerdict() { return consensusVerdict; }
    public String getMitigationAction() { return mitigationAction; }
}
