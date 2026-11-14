package com.cybershield.ai.feature.agents.orchestrator.strategy;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public class BatterySaverStrategy implements OrchestrationStrategy {

    @Override
    public AgentAction determineAgentAction(String agentId, SystemMetrics metrics) {
        if (AppConstants.AGENT_PHISHING.equals(agentId)) {
            return AgentAction.PAUSE;
        }
        return AgentAction.SLEEP;
    }

    @Override
    public String getStrategyName() {
        return "BATTERY_SAVER";
    }
}
