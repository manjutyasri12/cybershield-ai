package com.cybershield.ai.feature.agents.app_security.model;

import androidx.annotation.NonNull;

import java.util.List;

public class AppSecurityReport {

    private final int totalAppsScanned;
    private final int highRiskAppsCount;
    private final AppThreatLevel overallThreatLevel;
    private final String recommendation;
    private final List<String> flaggedPackageNames;
    private final List<String> vulnerabilityDetails;
    private final long timestamp;

    public AppSecurityReport(
            int totalAppsScanned,
            int highRiskAppsCount,
            @NonNull AppThreatLevel overallThreatLevel,
            @NonNull String recommendation,
            @NonNull List<String> flaggedPackageNames,
            @NonNull List<String> vulnerabilityDetails,
            long timestamp
    ) {
        this.totalAppsScanned = totalAppsScanned;
        this.highRiskAppsCount = highRiskAppsCount;
        this.overallThreatLevel = overallThreatLevel;
        this.recommendation = recommendation;
        this.flaggedPackageNames = flaggedPackageNames;
        this.vulnerabilityDetails = vulnerabilityDetails;
        this.timestamp = timestamp;
    }

    public int getTotalAppsScanned() { return totalAppsScanned; }
    public int getHighRiskAppsCount() { return highRiskAppsCount; }
    @NonNull
    public AppThreatLevel getOverallThreatLevel() { return overallThreatLevel; }
    @NonNull
    public String getRecommendation() { return recommendation; }
    @NonNull
    public List<String> getFlaggedPackageNames() { return flaggedPackageNames; }
    @NonNull
    public List<String> getVulnerabilityDetails() { return vulnerabilityDetails; }
    public long getTimestamp() { return timestamp; }
}
