package com.cybershield.ai.feature.agents.phishing;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.feature.agents.base.AgentResult;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;

import java.util.Locale;

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

            float phishingRisk;
            boolean modelAvailable = tfLiteEngine.isModelLoaded();
            if (modelAvailable) {
                float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 2);
                float[] probabilities = tensorUtils.softmax(outputTensor[0]);
                phishingRisk = probabilities[1];
            } else {
                phishingRisk = estimateHeuristicRisk(url);
            }

            boolean isThreat = phishingRisk >= AppConstants.THREAT_THRESHOLD_MEDIUM;
            String summary = isThreat
                    ? "Phishing URL pattern detected with risk score: " + String.format(Locale.ROOT, "%.2f", phishingRisk)
                    : modelAvailable
                    ? "URL verified clean"
                    : "No on-device model is loaded; no high-risk URL indicators were found.";

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

    private float estimateHeuristicRisk(@NonNull String url) {
        Uri uri = Uri.parse(url.trim());
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String normalizedUrl = url.toLowerCase(Locale.ROOT);

        if (host == null || host.isEmpty() || uri.getUserInfo() != null) {
            return 0.85f;
        }

        float risk = "http".equalsIgnoreCase(scheme) ? 0.30f : 0.0f;
        if (host.startsWith("xn--") || host.contains("bit.ly") || host.contains("tinyurl")) {
            risk += 0.35f;
        }
        if (normalizedUrl.contains("verify") || normalizedUrl.contains("login")
                || normalizedUrl.contains("update") || normalizedUrl.contains("secure")) {
            risk += 0.20f;
        }
        return Math.min(risk, 1.0f);
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        // Handle state transition log or resource reallocation
    }
}
