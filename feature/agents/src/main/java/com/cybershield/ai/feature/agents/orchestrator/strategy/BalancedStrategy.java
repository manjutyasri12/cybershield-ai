package com.cybershield.ai.feature.agents.orchestrator.strategy;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public class BalancedStrategy implements OrchestrationStrategy {

    @Override
    public AgentAction determineAgentAction(String agentId, SystemMetrics metrics) {
        if (AppConstants.AGENT_PHISHING.equals(agentId) || AppConstants.AGENT_NETWORK.equals(agentId)) {
            return AgentAction.RESUME;
        } else {
            return AgentAction.PAUSE;
        }
    }

    @Override
    public String getStrategyName() {
        return "BALANCED";
    }
}
