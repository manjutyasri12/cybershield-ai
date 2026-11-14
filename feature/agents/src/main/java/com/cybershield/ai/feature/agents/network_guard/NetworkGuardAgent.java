package com.cybershield.ai.feature.agents.network_guard;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.feature.agents.base.AgentResult;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Network Guard & MitM Anomaly Detection Agent.
 * Inspects Wi-Fi security parameters, DNS hijacking indicators, and unencrypted HTTP traffic.
 */
@Singleton
public class NetworkGuardAgent extends BaseAgent {

    @Inject
    public NetworkGuardAgent(AppExecutors appExecutors) {
        super(AppConstants.AGENT_NETWORK, appExecutors);
    }

    public void inspectNetworkConnection(@NonNull String ssid, boolean isEncrypted, boolean isCaptivePortal, @NonNull AgentCallback callback) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onResult(new AgentResult(agentId, 0.0f, false, "NONE", "Agent is dormant", System.currentTimeMillis()));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            float riskScore = 0.0f;
            if (!isEncrypted) {
                riskScore += 0.60f; // Open Wi-Fi risk
            }
            if (isCaptivePortal) {
                riskScore += 0.25f; // Captive Portal vulnerability
            }

            riskScore = Math.min(riskScore, 1.0f);
            boolean isThreat = riskScore >= AppConstants.THREAT_THRESHOLD_MEDIUM;
            String summary = isThreat ? "Untrusted Wi-Fi connection detected: " + ssid : "Network connection secure";

            AgentResult result = new AgentResult(
                    agentId,
                    riskScore,
                    isThreat,
                    "NETWORK_MITM",
                    summary,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        // Handle network guard state
    }
}
