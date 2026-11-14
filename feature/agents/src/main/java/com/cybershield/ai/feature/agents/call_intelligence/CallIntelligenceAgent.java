package com.cybershield.ai.feature.agents.call_intelligence;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;
import com.cybershield.ai.feature.agents.call_intelligence.model.CallIntelligenceResult;
import com.cybershield.ai.feature.agents.call_intelligence.model.CallRecommendation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Call Intelligence Agent.
 * Analyzes incoming calls for Spam, Scam, Unknown Numbers, Voice Scam Patterns,
 * and Call Frequency spikes using 100% local on-device TFLite ML inference.
 * Zero audio or transcript data leaves the device.
 */
@Singleton
public class CallIntelligenceAgent extends BaseAgent {

    private final TFLiteEngine tfLiteEngine;
    private final FeatureVectorizer featureVectorizer;
    private final TensorUtils tensorUtils;
    private final DataRedactor dataRedactor;

    // Local call frequency tracker: Map<PhoneNumber, CallCount>
    private final Map<String, Integer> callFrequencyMap = new ConcurrentHashMap<>();

    @Inject
    public CallIntelligenceAgent(
            AppExecutors appExecutors,
            TFLiteEngine tfLiteEngine,
            FeatureVectorizer featureVectorizer,
            TensorUtils tensorUtils,
            DataRedactor dataRedactor
    ) {
        super(AppConstants.AGENT_CALL_INTELLIGENCE, appExecutors);
        this.tfLiteEngine = tfLiteEngine;
        this.featureVectorizer = featureVectorizer;
        this.tensorUtils = tensorUtils;
        this.dataRedactor = dataRedactor;
    }

    public interface CallAnalysisCallback {
        void onResult(CallIntelligenceResult result);
    }

    public void analyzeIncomingCall(
            @NonNull String rawPhoneNumber,
            boolean isContactSaved,
            @NonNull String speechToTextSnippet,
            @NonNull CallAnalysisCallback callback
    ) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onResult(new CallIntelligenceResult(
                    0.0f,
                    0.5f,
                    "Agent dormant to preserve power",
                    CallRecommendation.ALLOW_CALL,
                    rawPhoneNumber,
                    !isContactSaved,
                    1,
                    System.currentTimeMillis()
            ));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            int frequency = updateAndGetFrequency(rawPhoneNumber);
            float[] featureArray = featureVectorizer.vectorizeCallSignals(rawPhoneNumber, frequency, speechToTextSnippet);
            float[][] inputTensor = new float[][]{featureArray};

            // Run TFLite inference (Output classes: [Clean, Spam, Scam])
            float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 3);
            float[] probabilities = tensorUtils.softmax(outputTensor[0]);

            float cleanProb = probabilities[0];
            float spamProb = probabilities[1];
            float scamProb = probabilities[2];

            float riskScore = Math.max(spamProb, scamProb);
            float confidenceScore = Math.max(cleanProb, riskScore);

            CallRecommendation recommendation;
            StringBuilder explanationBuilder = new StringBuilder();

            boolean isUnknown = !isContactSaved;
            if (isUnknown) {
                explanationBuilder.append("Unknown number. ");
            }

            if (frequency > 5) {
                riskScore = Math.min(riskScore + 0.35f, 1.0f);
                explanationBuilder.append("High call frequency anomaly (Robocall pattern). ");
            }

            if (scamProb >= AppConstants.THREAT_THRESHOLD_HIGH) {
                recommendation = CallRecommendation.BLOCK_CALL;
                explanationBuilder.append("High confidence Voice Scam pattern detected.");
            } else if (riskScore >= AppConstants.THREAT_THRESHOLD_MEDIUM) {
                recommendation = CallRecommendation.SILENCE_CALL;
                explanationBuilder.append("Likely Spam / Telemarketer call detected.");
            } else if (isUnknown && riskScore >= AppConstants.THREAT_THRESHOLD_LOW) {
                recommendation = CallRecommendation.WARN_USER;
                explanationBuilder.append("Unverified caller profile.");
            } else {
                recommendation = CallRecommendation.ALLOW_CALL;
                explanationBuilder.append("Call pattern verified safe.");
            }

            String sanitizedExplanation = dataRedactor.redactPii(explanationBuilder.toString());

            CallIntelligenceResult result = new CallIntelligenceResult(
                    riskScore,
                    confidenceScore,
                    sanitizedExplanation,
                    recommendation,
                    rawPhoneNumber,
                    isUnknown,
                    frequency,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    private int updateAndGetFrequency(String phoneNumber) {
        int currentCount = callFrequencyMap.getOrDefault(phoneNumber, 0) + 1;
        callFrequencyMap.put(phoneNumber, currentCount);
        return currentCount;
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        if (newState == AgentState.DORMANT) {
            callFrequencyMap.clear();
        }
    }
}
