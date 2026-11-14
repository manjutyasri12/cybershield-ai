package com.cybershield.ai.feature.agents.app_security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.app_security.model.AppSecurityReport;
import com.cybershield.ai.feature.agents.app_security.model.AppThreatLevel;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * App Security Agent.
 * Scans installed apps, permission profiles, Accessibility Service abuse,
 * Overlay permissions, Unknown APK sources, Fake Banking Apps, and Risky Background Services.
 * 100% Privacy-First: All scanning and ML inference occur on-device.
 */
@Singleton
public class AppSecurityAgent extends BaseAgent {

    private final Context context;
    private final TFLiteEngine tfLiteEngine;
    private final FeatureVectorizer featureVectorizer;
    private final TensorUtils tensorUtils;
    private final DataRedactor dataRedactor;

    @Inject
    public AppSecurityAgent(
            Context context,
            AppExecutors appExecutors,
            TFLiteEngine tfLiteEngine,
            FeatureVectorizer featureVectorizer,
            TensorUtils tensorUtils,
            DataRedactor dataRedactor
    ) {
        super(AppConstants.AGENT_APP_SECURITY, appExecutors);
        this.context = context.getApplicationContext();
        this.tfLiteEngine = tfLiteEngine;
        this.featureVectorizer = featureVectorizer;
        this.tensorUtils = tensorUtils;
        this.dataRedactor = dataRedactor;
    }

    public interface ScanReportCallback {
        void onReportGenerated(AppSecurityReport report);
    }

    public void scanInstalledApplications(@NonNull ScanReportCallback callback) {
        if (getCurrentState() == AgentState.DORMANT) {
            callback.onReportGenerated(new AppSecurityReport(
                    0,
                    0,
                    AppThreatLevel.SAFE,
                    "Agent dormant to preserve battery",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    System.currentTimeMillis()
            ));
            return;
        }

        appExecutors.aiExecutor().execute(() -> {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_SERVICES);

            int totalApps = 0;
            int highRiskCount = 0;
            List<String> flaggedPackages = new ArrayList<>();
            List<String> vulnerabilityDetails = new ArrayList<>();
            float maxRiskScore = 0.0f;

            for (PackageInfo pkg : installedPackages) {
                // Ignore self app
                if (pkg.packageName.equals(context.getPackageName())) {
                    continue;
                }

                // Focus primarily on non-system user-installed apps or updated system apps
                boolean isSystemApp = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean isUpdatedSystemApp = (pkg.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

                if (isSystemApp && !isUpdatedSystemApp) {
                    continue;
                }

                totalApps++;

                boolean hasAccessibility = false;
                boolean hasOverlay = false;
                boolean hasSms = false;
                boolean hasBackgroundServices = (pkg.services != null && pkg.services.length > 0);

                if (pkg.requestedPermissions != null) {
                    for (String perm : pkg.requestedPermissions) {
                        if ("android.permission.BIND_ACCESSIBILITY_SERVICE".equals(perm)) {
                            hasAccessibility = true;
                        }
                        if ("android.permission.SYSTEM_ALERT_WINDOW".equals(perm)) {
                            hasOverlay = true;
                        }
                        if ("android.permission.RECEIVE_SMS".equals(perm) || "android.permission.READ_SMS".equals(perm)) {
                            hasSms = true;
                        }
                    }
                }

                // Check Sideload / Unknown installer source
                boolean isSideloaded = false;
                try {
                    String installer = pm.getInstallerPackageName(pkg.packageName);
                    if (installer == null || !installer.contains("vending")) {
                        isSideloaded = true; // Installed from unknown APK source outside Google Play
                    }
                } catch (Exception ignored) {
                }

                // Check Fake Banking App signature pattern
                String lowerPkg = pkg.packageName.toLowerCase();
                boolean isFakeBankingPattern = (lowerPkg.contains("sbi") || lowerPkg.contains("paytm") || lowerPkg.contains("hdfc") || lowerPkg.contains("icici") || lowerPkg.contains("gpay"))
                        && !lowerPkg.startsWith("com.sbi") && !lowerPkg.startsWith("com.paytm") && !lowerPkg.startsWith("com.hdfc") && !lowerPkg.startsWith("com.icici");

                // Vectorize app features
                float[] featureArray = featureVectorizer.vectorizeAppPermissions(
                        pkg.packageName,
                        hasAccessibility,
                        hasOverlay,
                        isSideloaded,
                        isFakeBankingPattern,
                        hasSms,
                        hasBackgroundServices
                );

                float[][] inputTensor = new float[][]{featureArray};
                float[][] outputTensor = tfLiteEngine.runInference(inputTensor, 2);
                float[] probabilities = tensorUtils.softmax(outputTensor[0]);
                float appRiskScore = probabilities[1];

                // Heuristic risk boosting for severe vulnerability combinations
                if (hasAccessibility && hasOverlay) {
                    appRiskScore = Math.min(appRiskScore + 0.50f, 1.0f);
                    vulnerabilityDetails.add(pkg.packageName + ": Severe Overlay + Accessibility Service Abuse detected.");
                } else if (hasAccessibility) {
                    appRiskScore = Math.min(appRiskScore + 0.35f, 1.0f);
                    vulnerabilityDetails.add(pkg.packageName + ": Accessibility Service Binding active.");
                }

                if (isFakeBankingPattern) {
                    appRiskScore = Math.min(appRiskScore + 0.60f, 1.0f);
                    vulnerabilityDetails.add(pkg.packageName + ": Fake Banking App spoofing pattern detected.");
                }

                if (isSideloaded && (hasOverlay || hasSms)) {
                    appRiskScore = Math.min(appRiskScore + 0.30f, 1.0f);
                    vulnerabilityDetails.add(pkg.packageName + ": Unknown APK source with sensitive permissions.");
                }

                if (appRiskScore >= AppConstants.THREAT_THRESHOLD_MEDIUM) {
                    highRiskCount++;
                    flaggedPackages.add(pkg.packageName);
                }

                if (appRiskScore > maxRiskScore) {
                    maxRiskScore = appRiskScore;
                }
            }

            AppThreatLevel overallThreatLevel;
            String recommendation;

            if (maxRiskScore >= 0.85f || highRiskCount >= 3) {
                overallThreatLevel = AppThreatLevel.CRITICAL_RISK;
                recommendation = "UNINSTALL_IMMEDIATELY: High-risk malware or fake banking apps detected.";
            } else if (maxRiskScore >= AppConstants.THREAT_THRESHOLD_MEDIUM || highRiskCount > 0) {
                overallThreatLevel = AppThreatLevel.HIGH_RISK;
                recommendation = "REVOKE_ACCESSIBILITY: Review accessibility & overlay permissions for flagged applications.";
            } else if (maxRiskScore >= AppConstants.THREAT_THRESHOLD_LOW) {
                overallThreatLevel = AppThreatLevel.MEDIUM_RISK;
                recommendation = "DISABLE_OVERLAY_PERMISSION: Inspect background overlay permissions.";
            } else if (totalApps > 0) {
                overallThreatLevel = AppThreatLevel.SAFE;
                recommendation = "NO_ACTION_REQUIRED: All installed applications audited clean.";
            } else {
                overallThreatLevel = AppThreatLevel.SAFE;
                recommendation = "NO_ACTION_REQUIRED: No suspicious application behavior found.";
            }

            AppSecurityReport report = new AppSecurityReport(
                    totalApps,
                    highRiskCount,
                    overallThreatLevel,
                    dataRedactor.redactPii(recommendation),
                    flaggedPackages,
                    vulnerabilityDetails,
                    System.currentTimeMillis()
            );

            appExecutors.mainThread().execute(() -> callback.onReportGenerated(report));
        });
    }

    @Override
    protected void onStateChanged(AgentState newState) {
        // Handle App Security Agent state transitions
    }
}
