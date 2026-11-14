package com.cybershield.ai.feature.agents.di;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.feature.agents.malware.MalwareAnomalyAgent;
import com.cybershield.ai.feature.agents.network_guard.NetworkGuardAgent;
import com.cybershield.ai.feature.agents.orchestrator.AgentOrchestrator;
import com.cybershield.ai.feature.agents.orchestrator.BatteryPowerMonitor;
import com.cybershield.ai.feature.agents.phishing.PhishingDetectorAgent;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class AgentsModule {

    @Provides
    @Singleton
    public static BatteryPowerMonitor provideBatteryPowerMonitor() {
        return new BatteryPowerMonitor();
    }

    @Provides
    @Singleton
    public static PhishingDetectorAgent providePhishingDetectorAgent(AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils) {
        return new PhishingDetectorAgent(appExecutors, tfLiteEngine, featureVectorizer, tensorUtils);
    }

    @Provides
    @Singleton
    public static MalwareAnomalyAgent provideMalwareAnomalyAgent(AppExecutors appExecutors) {
        return new MalwareAnomalyAgent(appExecutors);
    }

    @Provides
    @Singleton
    public static NetworkGuardAgent provideNetworkGuardAgent(AppExecutors appExecutors) {
        return new NetworkGuardAgent(appExecutors);
    }

    @Provides
    @Singleton
    public static AgentOrchestrator provideAgentOrchestrator(PhishingDetectorAgent phishingAgent, MalwareAnomalyAgent malwareAgent, NetworkGuardAgent networkAgent, BatteryPowerMonitor batteryPowerMonitor) {
        return new AgentOrchestrator(phishingAgent, malwareAgent, networkAgent, batteryPowerMonitor);
    }
}
