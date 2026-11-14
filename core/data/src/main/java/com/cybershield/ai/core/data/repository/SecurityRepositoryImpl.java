package com.cybershield.ai.core.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.cybershield.ai.core.common.base.BaseRepository;
import com.cybershield.ai.core.common.result.Resource;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.database.dao.SecurityScanDao;
import com.cybershield.ai.core.database.dao.ThreatEventDao;
import com.cybershield.ai.core.database.entity.SecurityScan;
import com.cybershield.ai.core.database.entity.ThreatEvent;
import com.cybershield.ai.core.network.api.FogNodeApi;
import com.cybershield.ai.core.network.model.FogPayload;
import com.cybershield.ai.core.network.model.FogResponse;
import com.cybershield.ai.core.security.CryptoManager;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.base.AgentResult;
import com.cybershield.ai.feature.agents.orchestrator.AgentOrchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class SecurityRepositoryImpl extends BaseRepository implements SecurityRepository {

    private final ThreatEventDao threatEventDao;
    private final SecurityScanDao securityScanDao;
    private final CryptoManager cryptoManager;
    private final DataRedactor dataRedactor;
    private final FogNodeApi fogNodeApi;
    private final AgentOrchestrator agentOrchestrator;

    @Inject
    public SecurityRepositoryImpl(
            AppExecutors appExecutors,
            ThreatEventDao threatEventDao,
            SecurityScanDao securityScanDao,
            CryptoManager cryptoManager,
            DataRedactor dataRedactor,
            FogNodeApi fogNodeApi,
            AgentOrchestrator agentOrchestrator
    ) {
        super(appExecutors);
        this.threatEventDao = threatEventDao;
        this.securityScanDao = securityScanDao;
        this.cryptoManager = cryptoManager;
        this.dataRedactor = dataRedactor;
        this.fogNodeApi = fogNodeApi;
        this.agentOrchestrator = agentOrchestrator;
    }

    @Override
    public LiveData<List<ThreatEvent>> getAllThreatLogs() {
        return threatEventDao.getAllThreatsLiveData();
    }

    @Override
    public LiveData<List<ThreatEvent>> getUnmitigatedThreatLogs() {
        return threatEventDao.getUnmitigatedThreatsLiveData();
    }

    @Override
    public LiveData<SecurityScan> getLatestSecurityScan() {
        return securityScanDao.getLatestScanLiveData();
    }

    @Override
    public void recordAgentResult(@NonNull AgentResult result) {
        appExecutors.diskIO().execute(() -> {
            if (result.isThreatDetected()) {
                String sanitizedSummary = dataRedactor.redactPii(result.getSummary());
                String encryptedDetails = cryptoManager.encrypt(result.getSummary() + " | Risk: " + result.getRiskScore());

                ThreatEvent event = new ThreatEvent(
                        result.getAgentId(),
                        result.getThreatType(),
                        result.getRiskScore(),
                        sanitizedSummary,
                        encryptedDetails,
                        result.getTimestamp(),
                        false
                );
                threatEventDao.insertThreat(event);
            }
        });
    }

    @Override
    public void executeFullSecurityAudit(@NonNull AuditCallback callback) {
        appExecutors.diskIO().execute(() -> {
            long startTime = System.currentTimeMillis();

            // Run audits across agents
            agentOrchestrator.getPhishingAgent().analyzeUrl("http://suspicious-verify-bank.top/login", result -> recordAgentResult(result));
            agentOrchestrator.getMalwareAnomalyAgent().analyzePackagePermissions("com.unknown.app", Collections.singletonList("android.permission.SYSTEM_ALERT_WINDOW"), result -> recordAgentResult(result));
            agentOrchestrator.getNetworkGuardAgent().inspectNetworkConnection("Free-Wifi-Guest", false, true, result -> recordAgentResult(result));

            long duration = System.currentTimeMillis() - startTime;
            int activeThreats = threatEventDao.getActiveThreatCountSync();
            String status = activeThreats > 0 ? "THREAT_DETECTED" : "CLEAN";

            SecurityScan scan = new SecurityScan(
                    System.currentTimeMillis(),
                    15,
                    activeThreats,
                    status,
                    duration
            );

            securityScanDao.insertScan(scan);

            appExecutors.mainThread().execute(() -> callback.onAuditComplete(Resource.success(scan)));
        });
    }

    @Override
    public void offloadThreatToFogNode(@NonNull AgentResult result, @NonNull FogCallback callback) {
        appExecutors.networkIO().execute(() -> {
            List<Float> dummyVector = new ArrayList<>();
            dummyVector.add(result.getRiskScore());
            dummyVector.add(result.isThreatDetected() ? 1.0f : 0.0f);

            String anonymizedHash = cryptoManager.encrypt(result.getAgentId() + result.getTimestamp());
            FogPayload payload = new FogPayload(result.getAgentId(), dummyVector, anonymizedHash, result.getTimestamp());

            try {
                Call<FogResponse> call = fogNodeApi.verifyThreat(payload);
                Response<FogResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    FogResponse fogResponse = response.body();
                    appExecutors.mainThread().execute(() -> callback.onFogResponse(Resource.success(fogResponse)));
                } else {
                    appExecutors.mainThread().execute(() -> callback.onFogResponse(Resource.error("Fog Node unreachable or invalid response", null)));
                }
            } catch (Exception e) {
                appExecutors.mainThread().execute(() -> callback.onFogResponse(Resource.error("Fog network communication error: " + e.getMessage(), e)));
            }
        });
    }

    @Override
    public void markThreatMitigated(long threatId) {
        appExecutors.diskIO().execute(() -> {
            List<ThreatEvent> threats = threatEventDao.getAllThreatsSync();
            for (ThreatEvent event : threats) {
                if (event.getId() == threatId) {
                    event.setMitigated(true);
                    threatEventDao.updateThreat(event);
                    break;
                }
            }
        });
    }

    @Override
    public void clearAllHistory() {
        appExecutors.diskIO().execute(() -> {
            threatEventDao.clearAllThreats();
            securityScanDao.clearAllScans();
        });
    }
}
