package com.cybershield.ai.feature.agents.sms_security;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;
import com.cybershield.ai.feature.agents.sms_security.model.SmsCategory;
import com.cybershield.ai.feature.agents.sms_security.model.SmsSecurityResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * SMS Security Agent.
 * Analyzes incoming SMS messages for OTP Scams, Phishing, Bank Fraud, Malicious URLs,
 * Urgency Panic Messages, Lottery Scams, Courier Scams, KYC Scams, Insurance Scams,
 * and Electricity Bill Scams using local NLP and on-device TensorFlow Lite execution.
 * 100% Privacy-First: Zero cloud processing.
 */
@Singleton
public class SmsSecurityAgent extends BaseAgent {

    private final TFLiteEngine tfLiteEngine;
    private final FeatureVectorizer featureVectorizer;
    private final TensorUtils tensorUtils;
    private final DataRedactor dataRedactor;

    @Inject
    public SmsSecurityAgent(
            AppExecutors appExecutors,
            TFLiteEngine tfLiteEngine,
            FeatureVectorizer featureVectorizer,
            TensorUtils tensorUtils,
            DataRedactor dataRedactor
    ) {
        super(AppConstants.AGENT_SMS_SECURITY, appExecutors);
        this.tfLiteEngine = tfLiteEngine;
        this.featureVectorizer = featureVectorizer;
        this.tensorUtils = tensorUtils;
        this.dataRedactor = dataRedactor;
    }

    public interface SmsAnalysisCallback {
        void onResult(SmsSecurityResult result);
    }

    public void analyzeSmsMessage(
            @NonNull String sender,
            @NonNull String messageBody,
            @NonNull SmsAnalysisCallback callback
    ) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onResult(new SmsSecurityResult(
                    0.0f,
                    0.5f,
                    SmsCategory.CLEAN,
                    "Agent dormant to preserve battery",
                    sender,
                    dataRedactor.redactPii(messageBody),
                    System.currentTimeMillis()
            ));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            float[] featureArray = featureVectorizer.vectorizeSmsText(sender, messageBody);
            float[][] inputTensor = new float[][]{featureArray};

            // Run local TFLite inference (2 classes: [Safe, Fraudulent])
            float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 2);
            float[] probabilities = tensorUtils.softmax(outputTensor[0]);

            float safeProb = probabilities[0];
            float fraudProb = probabilities[1];

            float threatScore = fraudProb;
            float confidenceScore = Math.max(safeProb, fraudProb);

            String lowerBody = messageBody.toLowerCase();
            SmsCategory category = SmsCategory.CLEAN;
            StringBuilder reasonBuilder = new StringBuilder();

            // Heuristic NLP Category Categorization & Threat Score Boosting
            if (lowerBody.contains("electricity") && (lowerBody.contains("disconnect") || lowerBody.contains("unpaid"))) {
                category = SmsCategory.ELECTRICITY_BILL_SCAM;
                threatScore = Math.min(threatScore + 0.50f, 1.0f);
                reasonBuilder.append("Electricity bill disconnection scam pattern detected. ");
            } else if (lowerBody.contains("kyc") || lowerBody.contains("pan card") || lowerBody.contains("sim block")) {
                category = SmsCategory.KYC_SCAM;
                threatScore = Math.min(threatScore + 0.45f, 1.0f);
                reasonBuilder.append("Suspicious KYC / SIM blocking trap detected. ");
            } else if (lowerBody.contains("courier") || lowerBody.contains("package") || lowerBody.contains("delivery failed")) {
                category = SmsCategory.COURIER_SCAM;
                threatScore = Math.min(threatScore + 0.40f, 1.0f);
                reasonBuilder.append("Fake courier delivery address update scam detected. ");
            } else if (lowerBody.contains("lottery") || lowerBody.contains("winner") || lowerBody.contains("prize")) {
                category = SmsCategory.LOTTERY_SCAM;
                threatScore = Math.min(threatScore + 0.45f, 1.0f);
                reasonBuilder.append("Lottery / Prize money claim scam detected. ");
            } else if (lowerBody.contains("insurance") || lowerBody.contains("policy maturity") || lowerBody.contains("premium refund")) {
                category = SmsCategory.INSURANCE_SCAM;
                threatScore = Math.min(threatScore + 0.40f, 1.0f);
                reasonBuilder.append("Insurance policy refund / bonus scam pattern detected. ");
            } else if (lowerBody.contains("otp") || lowerBody.contains("share code") || lowerBody.contains("pin")) {
                category = SmsCategory.OTP_SCAM;
                threatScore = Math.min(threatScore + 0.40f, 1.0f);
                reasonBuilder.append("OTP / Security PIN request scam pattern detected. ");
            } else if (lowerBody.contains("account blocked") || lowerBody.contains("unfreeze") || lowerBody.contains("debit card")) {
                category = SmsCategory.BANK_FRAUD;
                threatScore = Math.min(threatScore + 0.35f, 1.0f);
                reasonBuilder.append("Fake banking alert / account suspension fraud detected. ");
            } else if (lowerBody.contains("http://") || lowerBody.contains("https://") || lowerBody.contains("bit.ly") || lowerBody.contains(".apk")) {
                category = SmsCategory.MALICIOUS_URL;
                threatScore = Math.min(threatScore + 0.35f, 1.0f);
                reasonBuilder.append("Malicious URL / unverified APK download link detected in SMS text. ");
            } else if (lowerBody.contains("urgent") || lowerBody.contains("immediately") || lowerBody.contains("24 hours")) {
                category = SmsCategory.URGENCY_MESSAGE;
                threatScore = Math.min(threatScore + 0.25f, 1.0f);
                reasonBuilder.append("High urgency panic manipulation message detected. ");
            } else if (threatScore >= AppConstants.THREAT_THRESHOLD_MEDIUM) {
                category = SmsCategory.PHISHING;
                reasonBuilder.append("General SMS phishing pattern detected.");
            } else {
                category = SmsCategory.CLEAN;
                reasonBuilder.append("SMS message verified safe.");
            }

            String redactedBodySnippet = dataRedactor.redactPii(messageBody);
            String sanitizedReason = dataRedactor.redactPii(reasonBuilder.toString());

            SmsSecurityResult result = new SmsSecurityResult(
                    threatScore,
                    confidenceScore,
                    category,
                    sanitizedReason,
                    sender,
                    redactedBodySnippet,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        // Handle SMS agent state transitions
    }
}
