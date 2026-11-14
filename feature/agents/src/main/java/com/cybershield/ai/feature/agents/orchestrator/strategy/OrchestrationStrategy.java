package com.cybershield.ai.feature.agents.orchestrator.strategy;

import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

public interface OrchestrationStrategy {
    AgentAction determineAgentAction(String agentId, SystemMetrics metrics);
    String getStrategyName();
}
