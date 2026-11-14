package com.cybershield.ai.feature.agents.orchestrator.observer;

import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public interface OrchestrationObserver {
    void onAgentStateChanged(String agentId, AgentAction action, SystemMetrics metrics);
}
