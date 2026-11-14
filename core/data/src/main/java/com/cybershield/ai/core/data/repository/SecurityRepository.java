package com.cybershield.ai.core.data.repository;

import androidx.lifecycle.LiveData;

import com.cybershield.ai.core.common.result.Resource;
import com.cybershield.ai.core.database.entity.SecurityScan;
import com.cybershield.ai.core.database.entity.ThreatEvent;
import com.cybershield.ai.core.network.model.FogResponse;
import com.cybershield.ai.feature.agents.base.AgentResult;

import java.util.List;

public interface SecurityRepository {

    LiveData<List<ThreatEvent>> getAllThreatLogs();

    LiveData<List<ThreatEvent>> getUnmitigatedThreatLogs();

    LiveData<SecurityScan> getLatestSecurityScan();

    void recordAgentResult(AgentResult result);

    void executeFullSecurityAudit(AuditCallback callback);

    void offloadThreatToFogNode(AgentResult result, FogCallback callback);

    void markThreatMitigated(long threatId);

    void clearAllHistory();

    interface AuditCallback {
        void onAuditComplete(Resource<SecurityScan> result);
    }

    interface FogCallback {
        void onFogResponse(Resource<FogResponse> result);
    }
}
