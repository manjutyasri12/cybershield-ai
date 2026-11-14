package com.cybershield.ai.feature.agents.orchestrator.strategy;

import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public class EmergencyThrottleStrategy implements OrchestrationStrategy {

    @Override
    public AgentAction determineAgentAction(String agentId, SystemMetrics metrics) {
        return AgentAction.SLEEP;
    }

    @Override
    public String getStrategyName() {
        return "EMERGENCY_THROTTLE";
    }
}
