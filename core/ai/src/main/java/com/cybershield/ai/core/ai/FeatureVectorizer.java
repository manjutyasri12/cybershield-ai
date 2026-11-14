package com.cybershield.ai.core.ai;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Feature Vectorizer converts raw domain signals (URLs, text snippets, permission strings,
 * and network packet headers) into normalized float array Tensors for TFLite inference.
 */
@Singleton
public class FeatureVectorizer {

    private static final int VECTOR_SIZE_URL = 10;

    @Inject
    public FeatureVectorizer() {
    }

    @NonNull
    public float[] vectorizeUrl(@NonNull String url) {
        float[] features = new float[VECTOR_SIZE_URL];
        if (url == null || url.isEmpty()) {
            return features;
        }

        String lowerUrl = url.toLowerCase();

        // Feature 0: URL Length
        features[0] = Math.min(url.length() / 200.0f, 1.0f);
        // Feature 1: Has IP address in hostname
        features[1] = lowerUrl.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*") ? 1.0f : 0.0f;
        // Feature 2: Has @ symbol (Phishing trick)
        features[2] = lowerUrl.contains("@") ? 1.0f : 0.0f;
        // Feature 3: Has double slash redirection
        features[3] = lowerUrl.indexOf("//", 8) > 0 ? 1.0f : 0.0f;
        // Feature 4: Subdomain count
        int dotCount = lowerUrl.split("\\.").length - 1;
        features[4] = Math.min(dotCount / 5.0f, 1.0f);
        // Feature 5: HTTPS protocol presence
        features[5] = lowerUrl.startsWith("https://") ? 1.0f : 0.0f;
        // Feature 6: Suspicious TLD (.xyz, .top, .zip, .click)
        features[6] = (lowerUrl.contains(".xyz") || lowerUrl.contains(".top") || lowerUrl.contains(".zip") || lowerUrl.contains(".click")) ? 1.0f : 0.0f;
        // Feature 7: Presence of login/verify keywords
        features[7] = (lowerUrl.contains("login") || lowerUrl.contains("verify") || lowerUrl.contains("secure") || lowerUrl.contains("account")) ? 1.0f : 0.0f;
        // Feature 8: Hyphen count in domain
        int hyphenCount = lowerUrl.split("-").length - 1;
        features[8] = Math.min(hyphenCount / 3.0f, 1.0f);
        // Feature 9: Digit ratio
        int digitCount = 0;
        for (char c : lowerUrl.toCharArray()) {
            if (Character.isDigit(c)) digitCount++;
        }
        features[9] = (float) digitCount / Math.max(1, url.length());

        return features;
    }

    @NonNull
    public List<Float> toList(@NonNull float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float f : array) {
            list.add(f);
        }
        return list;
    }

    @NonNull
    public float[] vectorizeCallSignals(@NonNull String phoneNumber, int callFrequencyLastHour, @NonNull String transcriptSnippet) {
        float[] features = new float[10];
        if (phoneNumber == null) phoneNumber = "";
        if (transcriptSnippet == null) transcriptSnippet = "";

        // Feature 0: Unknown / Short code number format
        boolean isShortCode = phoneNumber.length() < 7;
        features[0] = isShortCode ? 1.0f : 0.0f;

        // Feature 1: High Call Frequency ratio (Robocall flood indicator)
        features[1] = Math.min(callFrequencyLastHour / 10.0f, 1.0f);

        // Feature 2: Contains international prefix without contact association
        features[2] = (phoneNumber.startsWith("+") && !phoneNumber.startsWith("+1")) ? 0.8f : 0.0f;

        // Features 3-7: Voice Scam pattern keyword indicators in transcriptSnippet
        String lowerSnippet = transcriptSnippet.toLowerCase();
        features[3] = (lowerSnippet.contains("bank") || lowerSnippet.contains("verify") || lowerSnippet.contains("account")) ? 1.0f : 0.0f;
        features[4] = (lowerSnippet.contains("urgent") || lowerSnippet.contains("immediate") || lowerSnippet.contains("lawsuit")) ? 1.0f : 0.0f;
        features[5] = (lowerSnippet.contains("gift card") || lowerSnippet.contains("crypto") || lowerSnippet.contains("wire transfer")) ? 1.0f : 0.0f;
        features[6] = (lowerSnippet.contains("irs") || lowerSnippet.contains("tax") || lowerSnippet.contains("social security")) ? 1.0f : 0.0f;
        features[7] = (lowerSnippet.contains("winner") || lowerSnippet.contains("lottery") || lowerSnippet.contains("prize")) ? 1.0f : 0.0f;

        // Feature 8: Suspicious Area Code pattern
        features[8] = (phoneNumber.contains("876") || phoneNumber.contains("809") || phoneNumber.contains("284")) ? 1.0f : 0.0f;

        // Feature 9: Digit entropy / repeating numbers
        features[9] = phoneNumber.matches(".*(\\d)\\1{4,}.*") ? 0.9f : 0.1f;

        return features;
    }

    @NonNull
    public float[] vectorizePaymentSignals(@NonNull String upiUri, double amount, boolean isNewBeneficiary, int failedAttemptsCount) {
        float[] features = new float[10];
        if (upiUri == null) upiUri = "";
        String lowerUri = upiUri.toLowerCase();

        // Feature 0: Valid UPI scheme (upi://pay)
        features[0] = lowerUri.startsWith("upi://pay") ? 1.0f : 0.0f;

        // Feature 1: High Transaction Amount normalized (Threshold e.g. >10,000 INR)
        features[1] = Math.min((float) (amount / 20000.0), 1.0f);

        // Feature 2: Is New Beneficiary
        features[2] = isNewBeneficiary ? 1.0f : 0.0f;

        // Feature 3: Failed Payment attempts frequency
        features[3] = Math.min(failedAttemptsCount / 5.0f, 1.0f);

        // Features 4-7: Fraudulent keywords in VPA or note (e.g. "cashback", "refund", "lottery", "support", "reward")
        features[4] = (lowerUri.contains("cashback") || lowerUri.contains("reward")) ? 1.0f : 0.0f;
        features[5] = (lowerUri.contains("refund") || lowerUri.contains("support")) ? 1.0f : 0.0f;
        features[6] = (lowerUri.contains("claim") || lowerUri.contains("offer")) ? 1.0f : 0.0f;
        features[7] = (lowerUri.contains("pin") || lowerUri.contains("verify")) ? 1.0f : 0.0f;

        // Feature 8: Malformed VPA format
        features[8] = (lowerUri.contains("pa=") && !lowerUri.contains("@")) ? 1.0f : 0.0f;

        // Feature 9: Spoofed domain extension in VPA
        features[9] = (lowerUri.contains("@ybl") || lowerUri.contains("@paytm") || lowerUri.contains("@okaxis") || lowerUri.contains("@upi")) ? 0.0f : 0.8f;

        return features;
    }

    @NonNull
    public float[] vectorizeSmsText(@NonNull String sender, @NonNull String body) {
        float[] features = new float[12];
        if (sender == null) sender = "";
        if (body == null) body = "";
        String lowerBody = body.toLowerCase();
        String lowerSender = sender.toLowerCase();

        // Feature 0: Contains URL link
        features[0] = (lowerBody.contains("http://") || lowerBody.contains("https://") || lowerBody.contains("bit.ly") || lowerBody.contains("tinyurl")) ? 1.0f : 0.0f;

        // Feature 1: OTP / PIN sharing request keywords
        features[1] = (lowerBody.contains("otp") || lowerBody.contains("pin") || lowerBody.contains("share code") || lowerBody.contains("verification code")) ? 1.0f : 0.0f;

        // Feature 2: Bank Fraud keywords
        features[2] = (lowerBody.contains("bank") || lowerBody.contains("account blocked") || lowerBody.contains("debit card") || lowerBody.contains("credit card") || lowerBody.contains("unfreeze")) ? 1.0f : 0.0f;

        // Feature 3: Urgency / Panic words
        features[3] = (lowerBody.contains("immediately") || lowerBody.contains("urgent") || lowerBody.contains("suspended") || lowerBody.contains("24 hours") || lowerBody.contains("today")) ? 1.0f : 0.0f;

        // Feature 4: Lottery / Prize words
        features[4] = (lowerBody.contains("lottery") || lowerBody.contains("winner") || lowerBody.contains("won") || lowerBody.contains("prize") || lowerBody.contains("lakh")) ? 1.0f : 0.0f;

        // Feature 5: Courier / Package delivery words
        features[5] = (lowerBody.contains("courier") || lowerBody.contains("package") || lowerBody.contains("delivery failed") || lowerBody.contains("update address") || lowerBody.contains("shipment")) ? 1.0f : 0.0f;

        // Feature 6: KYC / Aadhaar / PAN words
        features[6] = (lowerBody.contains("kyc") || lowerBody.contains("pan card") || lowerBody.contains("aadhaar") || lowerBody.contains("sim block")) ? 1.0f : 0.0f;

        // Feature 7: Insurance Scam words
        features[7] = (lowerBody.contains("insurance") || lowerBody.contains("policy maturity") || lowerBody.contains("premium refund") || lowerBody.contains("claim bonus")) ? 1.0f : 0.0f;

        // Feature 8: Electricity Bill disconnection words
        features[8] = (lowerBody.contains("electricity") || lowerBody.contains("power disconnection") || lowerBody.contains("bill unpaid") || lowerBody.contains("eb officer")) ? 1.0f : 0.0f;

        // Feature 9: Suspicious Sender ID format
        features[9] = (lowerSender.length() > 0 && !lowerSender.matches("^[a-z]{2}-[a-z]{6}$")) ? 0.8f : 0.0f;

        // Feature 10: Has APK download link
        features[10] = (lowerBody.contains(".apk") || lowerBody.contains("download link")) ? 1.0f : 0.0f;

        // Feature 11: Text length ratio
        features[11] = Math.min(body.length() / 160.0f, 1.0f);

        return features;
    }
}
