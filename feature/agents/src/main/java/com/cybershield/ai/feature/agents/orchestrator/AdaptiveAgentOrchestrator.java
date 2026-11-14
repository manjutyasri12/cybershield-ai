package com.cybershield.ai.feature.agents.orchestrator;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.base.BaseAgent;
import com.cybershield.ai.feature.agents.call_intelligence.CallIntelligenceAgent;
import com.cybershield.ai.feature.agents.malware.MalwareAnomalyAgent;
import com.cybershield.ai.feature.agents.network_guard.NetworkGuardAgent;
import com.cybershield.ai.feature.agents.orchestrator.model.AgentAction;
import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;
import com.cybershield.ai.feature.agents.orchestrator.monitor.SystemResourceMonitor;
import com.cybershield.ai.feature.agents.orchestrator.observer.OrchestrationObserver;
import com.cybershield.ai.feature.agents.orchestrator.strategy.BalancedStrategy;
import com.cybershield.ai.feature.agents.orchestrator.strategy.BatterySaverStrategy;
import com.cybershield.ai.feature.agents.orchestrator.strategy.EmergencyThrottleStrategy;
import com.cybershield.ai.feature.agents.orchestrator.strategy.OrchestrationStrategy;
import com.cybershield.ai.feature.agents.orchestrator.strategy.PerformanceStrategy;
import com.cybershield.ai.feature.agents.payment_protection.PaymentProtectionAgent;
import com.cybershield.ai.feature.agents.phishing.PhishingDetectorAgent;
import com.cybershield.ai.feature.agents.sms_security.SmsSecurityAgent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Adaptive Agent Orchestrator.
 * Implements Observer Pattern & Strategy Pattern to dynamically evaluate
 * CPU, RAM, Battery, Charging State, Network, and User Activity metrics.
 * Dynamically dispatches START, PAUSE, SLEEP, and RESUME commands to individual AI agents.
 */
@Singleton
public class AdaptiveAgentOrchestrator {

    private final SystemResourceMonitor resourceMonitor;
    private final PhishingDetectorAgent phishingAgent;
    private final MalwareAnomalyAgent malwareAgent;
    private final NetworkGuardAgent networkAgent;
    private final CallIntelligenceAgent callAgent;
    private final PaymentProtectionAgent paymentAgent;
    private final SmsSecurityAgent smsAgent;

    private final List<OrchestrationObserver> observers = new CopyOnWriteArrayList<>();
    private final MutableLiveData<String> activeStrategyName = new MutableLiveData<>("BALANCED");
    private final MutableLiveData<SystemMetrics> currentMetricsLiveData = new MutableLiveData<>();

    private OrchestrationStrategy currentStrategy;

    @Inject
    public AdaptiveAgentOrchestrator(
            SystemResourceMonitor resourceMonitor,
            PhishingDetectorAgent phishingAgent,
            MalwareAnomalyAgent malwareAgent,
            NetworkGuardAgent networkAgent,
            CallIntelligenceAgent callAgent,
            PaymentProtectionAgent paymentAgent,
            SmsSecurityAgent smsAgent
    ) {
        this.resourceMonitor = resourceMonitor;
        this.phishingAgent = phishingAgent;
        this.malwareAgent = malwareAgent;
        this.networkAgent = networkAgent;
        this.callAgent = callAgent;
        this.paymentAgent = paymentAgent;
        this.smsAgent = smsAgent;
        this.currentStrategy = new BalancedStrategy();
    }

    public void registerObserver(OrchestrationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregisterObserver(OrchestrationObserver observer) {
        observers.remove(observer);
    }

    public synchronized void evaluateAndDispatch() {
        SystemMetrics metrics = resourceMonitor.captureMetrics();
        currentMetricsLiveData.postValue(metrics);

        // Dynamically select Strategy based on System Metrics
        selectStrategy(metrics);

        // Dispatch Actions to Agents
        dispatchAgentAction(phishingAgent, metrics);
        dispatchAgentAction(malwareAgent, metrics);
        dispatchAgentAction(networkAgent, metrics);
        dispatchAgentAction(callAgent, metrics);
        dispatchAgentAction(paymentAgent, metrics);
        dispatchAgentAction(smsAgent, metrics);
    }

    private void selectStrategy(SystemMetrics metrics) {
        if (metrics.getBatteryLevelPercent() <= AppConstants.BATTERY_CRITICAL_LEVEL || metrics.getRamUsagePercent() > 90.0f) {
            currentStrategy = new EmergencyThrottleStrategy();
        } else if (metrics.isCharging() || (metrics.getBatteryLevelPercent() > 70 && !metrics.isUserActive())) {
            currentStrategy = new PerformanceStrategy();
        } else if (metrics.getBatteryLevelPercent() <= AppConstants.BATTERY_LOW_LEVEL) {
            currentStrategy = new BatterySaverStrategy();
        } else {
            currentStrategy = new BalancedStrategy();
        }

        activeStrategyName.postValue(currentStrategy.getStrategyName());
    }

    private void dispatchAgentAction(BaseAgent agent, SystemMetrics metrics) {
        AgentAction action = currentStrategy.determineAgentAction(agent.getAgentId(), metrics);
        applyActionToAgent(agent, action);

        // Notify Observers via Observer Pattern
        for (OrchestrationObserver observer : observers) {
            observer.onAgentStateChanged(agent.getAgentId(), action, metrics);
        }
    }

    private void applyActionToAgent(BaseAgent agent, AgentAction action) {
        switch (action) {
            case START:
            case RESUME:
                agent.setState(AgentState.ACTIVE);
                break;
            case PAUSE:
                agent.setState(AgentState.LOW_POWER_PERIODIC);
                break;
            case SLEEP:
                agent.setState(AgentState.DORMANT);
                break;
        }
    }

    public LiveData<String> getActiveStrategyName() {
        return activeStrategyName;
    }

    public LiveData<SystemMetrics> getCurrentMetricsLiveData() {
        return currentMetricsLiveData;
    }

    public PhishingDetectorAgent getPhishingAgent() { return phishingAgent; }
    public MalwareAnomalyAgent getMalwareAgent() { return malwareAgent; }
    public NetworkGuardAgent getNetworkAgent() { return networkAgent; }
    public CallIntelligenceAgent getCallAgent() { return callAgent; }
    public PaymentProtectionAgent getPaymentAgent() { return paymentAgent; }
    public SmsSecurityAgent getSmsAgent() { return smsAgent; }
}
