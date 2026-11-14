package com.cybershield.ai.core.network.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Anonymized feature vector payload offloaded to local Fog nodes
 * for high-compute security verification.
 */
public class FogPayload {

    @SerializedName("agent_id")
    private String agentId;

    @SerializedName("feature_vector")
    private List<Float> featureVector;

    @SerializedName("anonymized_hash")
    private String anonymizedHash;

    @SerializedName("timestamp")
    private long timestamp;

    public FogPayload(String agentId, List<Float> featureVector, String anonymizedHash, long timestamp) {
        this.agentId = agentId;
        this.featureVector = featureVector;
        this.anonymizedHash = anonymizedHash;
        this.timestamp = timestamp;
    }

    public String getAgentId() { return agentId; }
    public List<Float> getFeatureVector() { return featureVector; }
    public String getAnonymizedHash() { return anonymizedHash; }
    public long getTimestamp() { return timestamp; }
}
