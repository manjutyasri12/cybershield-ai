package com.cybershield.ai.feature.agents.orchestrator.strategy;

import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public class PerformanceStrategy implements OrchestrationStrategy {

    @Override
    public AgentAction determineAgentAction(String agentId, SystemMetrics metrics) {
        return AgentAction.RESUME;
    }

    @Override
    public String getStrategyName() {
        return "PERFORMANCE";
    }
}
