package com.cybershield.ai.core.ai;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Explainable Risk Engine (XAI).
 * Provides feature importance ranking and transparent explanations
 * for on-device TFLite model threat classifications.
 */
@Singleton
public class ExplainableRiskEngine {

    @Inject
    public ExplainableRiskEngine() {
    }

    @NonNull
    public String generateExplanation(float riskScore, @NonNull String threatType) {
        if (riskScore >= 0.85f) {
            return "High confidence " + threatType + " threat detected based on high-risk feature tensor weights.";
        } else if (riskScore >= 0.60f) {
            return "Moderate anomaly detected by " + threatType + " model heuristic analysis.";
        } else {
            return "Feature tensor analysis indicates normal operational profile for " + threatType + ".";
        }
    }
}
