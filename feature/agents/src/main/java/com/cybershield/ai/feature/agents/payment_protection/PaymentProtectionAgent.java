package com.cybershield.ai.feature.agents.payment_protection;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;
import com.cybershield.ai.feature.agents.payment_protection.model.PaymentAction;
import com.cybershield.ai.feature.agents.payment_protection.model.PaymentProtectionResult;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Payment Protection Agent.
 * Detects UPI Fraud, Fake QR payloads, New Beneficiaries, High Amount Spikes,
 * Behavioral Time Anomalies, and Repeated Failed Payments locally on-device.
 * 100% Privacy-First: No financial or account details leave the device.
 */
@Singleton
public class PaymentProtectionAgent extends BaseAgent {

    private final TFLiteEngine tfLiteEngine;
    private final FeatureVectorizer featureVectorizer;
    private final TensorUtils tensorUtils;
    private final DataRedactor dataRedactor;

    // Track failed attempts per VPA
    private final Map<String, Integer> failedPaymentAttempts = new ConcurrentHashMap<>();

    @Inject
    public PaymentProtectionAgent(
            AppExecutors appExecutors,
            TFLiteEngine tfLiteEngine,
            FeatureVectorizer featureVectorizer,
            TensorUtils tensorUtils,
            DataRedactor dataRedactor
    ) {
        super(AppConstants.AGENT_PAYMENT_PROTECTION, appExecutors);
        this.tfLiteEngine = tfLiteEngine;
        this.featureVectorizer = featureVectorizer;
        this.tensorUtils = tensorUtils;
        this.dataRedactor = dataRedactor;
    }

    public interface PaymentCallback {
        void onResult(PaymentProtectionResult result);
    }

    public void recordFailedPaymentAttempt(@NonNull String targetVpa) {
        failedPaymentAttempts.put(targetVpa, failedPaymentAttempts.getOrDefault(targetVpa, 0) + 1);
    }

    public void resetFailedPaymentAttempts(@NonNull String targetVpa) {
        failedPaymentAttempts.remove(targetVpa);
    }

    public void evaluatePaymentTransaction(
            @NonNull String upiPayloadOrQrUri,
            double amount,
            boolean isNewBeneficiary,
            @NonNull PaymentCallback callback
    ) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onResult(new PaymentProtectionResult(
                    0.0f,
                    0.5f,
                    "Payment agent dormant to preserve resources",
                    PaymentAction.ALLOW_TRANSACTION,
                    upiPayloadOrQrUri,
                    amount,
                    isNewBeneficiary,
                    0,
                    System.currentTimeMillis()
            ));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            int failedCount = failedPaymentAttempts.getOrDefault(upiPayloadOrQrUri, 0);

            float[] featureArray = featureVectorizer.vectorizePaymentSignals(upiPayloadOrQrUri, amount, isNewBeneficiary, failedCount);
            float[][] inputTensor = new float[][]{featureArray};

            // Run TFLite inference (Output classes: [Legitimate, Suspicious, Fraudulent])
            float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 3);
            float[] probabilities = tensorUtils.softmax(outputTensor[0]);

            float legitProb = probabilities[0];
            float suspProb = probabilities[1];
            float fraudProb = probabilities[2];

            float fraudScore = Math.max(suspProb, fraudProb);
            float confidenceScore = Math.max(legitProb, fraudScore);

            StringBuilder reasonBuilder = new StringBuilder();

            // Behavioral Time Anomaly check (Late night transaction flag 1 AM - 5 AM)
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            boolean isBehavioralTimeAnomaly = (hour >= 1 && hour <= 5);

            if (isBehavioralTimeAnomaly) {
                fraudScore = Math.min(fraudScore + 0.20f, 1.0f);
                reasonBuilder.append("Unusual late-night transaction hours detected. ");
            }

            if (failedCount >= 3) {
                fraudScore = Math.min(fraudScore + 0.35f, 1.0f);
                reasonBuilder.append("Repeated failed payment attempts detected (").append(failedCount).append(" failures). ");
            }

            if (isNewBeneficiary && amount > 10000.0) {
                fraudScore = Math.min(fraudScore + 0.25f, 1.0f);
                reasonBuilder.append("High amount transaction to a new unverified beneficiary. ");
            }

            String lowerPayload = upiPayloadOrQrUri.toLowerCase(Locale.ROOT);
            if (lowerPayload.contains("cashback") || lowerPayload.contains("refund") || lowerPayload.contains("claim")) {
                fraudScore = Math.min(fraudScore + 0.40f, 1.0f);
                reasonBuilder.append("Phishing keyword pattern (Cashback/Refund scheme) detected in QR payload. ");
            }

            if (lowerPayload.startsWith("http://") || lowerPayload.startsWith("https://")) {
                fraudScore = Math.min(fraudScore + 0.50f, 1.0f);
                reasonBuilder.append("Fake QR: QR payload resolves to web URL instead of direct valid UPI scheme. ");
            }

            PaymentAction action;
            if (fraudScore >= AppConstants.THREAT_THRESHOLD_HIGH) {
                action = PaymentAction.BLOCK_TRANSACTION;
                reasonBuilder.append("Critical UPI Fraud risk detected.");
            } else if (fraudScore >= AppConstants.THREAT_THRESHOLD_MEDIUM) {
                action = PaymentAction.REQUIRE_BIOMETRIC_VERIFICATION;
                reasonBuilder.append("Elevated risk profile. Biometric step-up authentication required.");
            } else if (fraudScore >= AppConstants.THREAT_THRESHOLD_LOW) {
                action = PaymentAction.WARN_USER;
                reasonBuilder.append("Unverified transaction parameters.");
            } else {
                action = PaymentAction.ALLOW_TRANSACTION;
                reasonBuilder.append("Transaction parameters audited safe.");
            }

            String sanitizedReason = dataRedactor.redactPii(reasonBuilder.toString());

            PaymentProtectionResult result = new PaymentProtectionResult(
                    fraudScore,
                    confidenceScore,
                    sanitizedReason,
                    action,
                    upiPayloadOrQrUri,
                    amount,
                    isNewBeneficiary,
                    failedCount,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        if (newState == AgentState.DORMANT) {
            failedPaymentAttempts.clear();
        }
    }
}
