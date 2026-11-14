package com.cybershield.ai.feature.agents.phishing;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.feature.agents.base.AgentResult;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Phishing Detection Agent.
 * Analyzes incoming URLs, domain structures, and text vectors for phishing patterns.
 */
@Singleton
public class PhishingDetectorAgent extends BaseAgent {

    private final TFLiteEngine tfLiteEngine;
    private final FeatureVectorizer featureVectorizer;
    private final TensorUtils tensorUtils;

    @Inject
    public PhishingDetectorAgent(AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils) {
        super(AppConstants.AGENT_PHISHING, appExecutors);
        this.tfLiteEngine = tfLiteEngine;
        this.featureVectorizer = featureVectorizer;
        this.tensorUtils = tensorUtils;
    }

    public void analyzeUrl(@NonNull String url, @NonNull AgentCallback callback) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onResult(new AgentResult(agentId, 0.0f, false, "NONE", "Agent is dormant to save power", System.currentTimeMillis()));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            float[] featureArray = featureVectorizer.vectorizeUrl(url);
            float[][] inputTensor = new float[][]{featureArray};

            // Run TFLite inference (2 output classes: [Safe, Phishing])
            float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 2);
            float[] probabilities = tensorUtils.softmax(outputTensor[0]);
            float phishingRisk = probabilities[1]; // Probability of phishing

            boolean isThreat = phishingRisk >= AppConstants.THREAT_THRESHOLD_MEDIUM;
            String summary = isThreat ? "Phishing URL pattern detected with risk score: " + String.format("%.2f", phishingRisk) : "URL verified clean";

            AgentResult result = new AgentResult(
                    agentId,
                    phishingRisk,
                    isThreat,
                    "PHISHING",
                    summary,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        // Handle state transition log or resource reallocation
    }
}
