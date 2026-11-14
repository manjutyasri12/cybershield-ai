package com.cybershield.ai.feature.agents.orchestrator;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cybershield.ai.feature.agents.base.AgentState;
import com.cybershield.ai.feature.agents.malware.MalwareAnomalyAgent;
import com.cybershield.ai.feature.agents.network_guard.NetworkGuardAgent;
import com.cybershield.ai.feature.agents.phishing.PhishingDetectorAgent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dynamic Multi-Agent Orchestrator.
 * Dynamically activates or deactivates agents based on battery level,
 * power policies, and threat triggers to minimize CPU & battery usage.
 */
@Singleton
public class AgentOrchestrator {

    private final PhishingDetectorAgent phishingAgent;
    private final MalwareAnomalyAgent malwareAgent;
    private final NetworkGuardAgent networkAgent;
    private final BatteryPowerMonitor batteryPowerMonitor;

    private final MutableLiveData<BatteryPowerMonitor.PowerPolicy> currentPolicyLiveData = new MutableLiveData<>(BatteryPowerMonitor.PowerPolicy.BALANCED);

    @Inject
    public AgentOrchestrator(PhishingDetectorAgent phishingAgent, MalwareAnomalyAgent malwareAgent, NetworkGuardAgent networkAgent, BatteryPowerMonitor batteryPowerMonitor) {
        this.phishingAgent = phishingAgent;
        this.malwareAgent = malwareAgent;
        this.networkAgent = networkAgent;
        this.batteryPowerMonitor = batteryPowerMonitor;
    }

    public void updateOrchestrationPolicy(@NonNull Context context) {
        BatteryPowerMonitor.PowerPolicy policy = batteryPowerMonitor.evaluatePowerPolicy(context);
        currentPolicyLiveData.postValue(policy);

        switch (policy) {
            case PERFORMANCE:
                phishingAgent.setState(AgentState.ACTIVE);
                malwareAgent.setState(AgentState.ACTIVE);
                networkAgent.setState(AgentState.ACTIVE);
                break;
            case BALANCED:
                phishingAgent.setState(AgentState.ACTIVE);
                malwareAgent.setState(AgentState.LOW_POWER_PERIODIC);
                networkAgent.setState(AgentState.ACTIVE);
                break;
            case SAVER:
                phishingAgent.setState(AgentState.LOW_POWER_PERIODIC);
                malwareAgent.setState(AgentState.DORMANT);
                networkAgent.setState(AgentState.LOW_POWER_PERIODIC);
                break;
        }
    }

    public LiveData<BatteryPowerMonitor.PowerPolicy> getCurrentPolicyLiveData() {
        return currentPolicyLiveData;
    }

    public PhishingDetectorAgent getPhishingAgent() { return phishingAgent; }
    public MalwareAnomalyAgent getMalwareAgent() { return malwareAgent; }
    public NetworkGuardAgent getNetworkAgent() { return networkAgent; }
}
