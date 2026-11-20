package com.cybershield.ai.core.ai;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unified on-device decision shown to the user.
 * Status is Safe, Suspicious, or Dangerous. Score and confidence are 0–100.
 */
public final class RiskAssessment {

    public static final String STATUS_SAFE = "Safe";
    public static final String STATUS_SUSPICIOUS = "Suspicious";
    public static final String STATUS_DANGEROUS = "Dangerous";

    public static final String ACTION_IGNORE = "Ignore";
    public static final String ACTION_VERIFY = "Verify";
    public static final String ACTION_BLOCK = "Block";
    public static final String ACTION_REPORT = "Report";

    private final ContentKind kind;
    private final int riskScore;
    private final int confidenceScore;
    private final String status;
    private final String category;
    private final String recommendedAction;
    private final String summary;
    private final List<String> reasons;

    public RiskAssessment(
            @NonNull ContentKind kind,
            int riskScore,
            int confidenceScore,
            @NonNull String status,
            @NonNull String category,
            @NonNull String recommendedAction,
            @NonNull String summary,
            @NonNull List<String> reasons
    ) {
        this.kind = kind;
        this.riskScore = riskScore;
        this.confidenceScore = confidenceScore;
        this.status = status;
        this.category = category;
        this.recommendedAction = recommendedAction;
        this.summary = summary;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
    }

    @NonNull
    public ContentKind getKind() {
        return kind;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getRecommendedAction() {
        return recommendedAction;
    }

    @NonNull
    public String getSummary() {
        return summary;
    }

    @NonNull
    public List<String> getReasons() {
        return reasons;
    }

    public boolean isThreat() {
        return !STATUS_SAFE.equals(status);
    }
}
