package com.cybershield.ai.feature.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.data.repository.SecurityRepository;
import com.cybershield.ai.core.database.entity.SecurityScan;
import com.cybershield.ai.core.database.entity.ThreatEvent;
import com.cybershield.ai.feature.agents.orchestrator.AdaptiveAgentOrchestrator;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final SecurityRepository securityRepository;
    private final AdaptiveAgentOrchestrator orchestrator;
    private final AppExecutors appExecutors;

    @Inject
    public DashboardViewModel(
            SecurityRepository securityRepository,
            AdaptiveAgentOrchestrator orchestrator,
            AppExecutors appExecutors
    ) {
        this.securityRepository = securityRepository;
        this.orchestrator = orchestrator;
        this.appExecutors = appExecutors;
    }

    public LiveData<List<ThreatEvent>> getThreatLogs() {
        return securityRepository.getAllThreatLogs();
    }

    public LiveData<SecurityScan> getLatestScan() {
        return securityRepository.getLatestSecurityScan();
    }

    public LiveData<String> getActiveStrategy() {
        return orchestrator.getActiveStrategyName();
    }

    public LiveData<SystemMetrics> getSystemMetrics() {
        return orchestrator.getCurrentMetricsLiveData();
    }

    public void triggerFullAudit(SecurityRepository.AuditCallback callback) {
        securityRepository.executeFullSecurityAudit(callback);
    }

    public void mitigateThreat(long threatId) {
        securityRepository.markThreatMitigated(threatId);
    }

    public void runOrchestratorEvaluation() {
        appExecutors.aiExecutor().execute(() -> orchestrator.evaluateAndDispatch());
    }
}
