package com.cybershield.ai.feature.agents.base;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.common.utils.AppExecutors;

/**
 * Abstract Base Agent for Multi-Agent AI Architecture.
 * Enforces standardized state control, execution scheduling, and state reporting.
 */
public abstract class BaseAgent {

    protected final String agentId;
    protected final AppExecutors appExecutors;
    private volatile AgentState currentState = AgentState.ACTIVE;

    public BaseAgent(@NonNull String agentId, @NonNull AppExecutors appExecutors) {
        this.agentId = agentId;
        this.appExecutors = appExecutors;
    }

    @NonNull
    public String getAgentId() {
        return agentId;
    }

    public AgentState getCurrentState() {
        return currentState;
    }

    public void setState(@NonNull AgentState state) {
        this.currentState = state;
        onStateChanged(state);
    }

    protected abstract void onStateChanged(AgentState newState);

    public interface AgentCallback {
        void onResult(AgentResult result);
    }
}
