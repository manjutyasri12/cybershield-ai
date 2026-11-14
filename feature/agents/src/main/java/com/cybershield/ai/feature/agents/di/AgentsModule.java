package com.cybershield.ai.feature.agents.di;

import android.content.Context;

import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.feature.agents.app_security.AppSecurityAgent;
import com.cybershield.ai.feature.agents.call_intelligence.CallIntelligenceAgent;
import com.cybershield.ai.feature.agents.malware.MalwareAnomalyAgent;
import com.cybershield.ai.feature.agents.network_guard.NetworkGuardAgent;
import com.cybershield.ai.feature.agents.orchestrator.AdaptiveAgentOrchestrator;
import com.cybershield.ai.feature.agents.orchestrator.BatteryPowerMonitor;
import com.cybershield.ai.feature.agents.orchestrator.monitor.SystemResourceMonitor;
import com.cybershield.ai.feature.agents.payment_protection.PaymentProtectionAgent;
import com.cybershield.ai.feature.agents.phishing.PhishingDetectorAgent;
import com.cybershield.ai.feature.agents.sms_security.SmsSecurityAgent;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class AgentsModule {

    @Provides
    @Singleton
    public static SystemResourceMonitor provideSystemResourceMonitor(@ApplicationContext Context context) {
        return new SystemResourceMonitor(context);
    }

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
    public static CallIntelligenceAgent provideCallIntelligenceAgent(AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils, DataRedactor dataRedactor) {
        return new CallIntelligenceAgent(appExecutors, tfLiteEngine, featureVectorizer, tensorUtils, dataRedactor);
    }

    @Provides
    @Singleton
    public static PaymentProtectionAgent providePaymentProtectionAgent(AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils, DataRedactor dataRedactor) {
        return new PaymentProtectionAgent(appExecutors, tfLiteEngine, featureVectorizer, tensorUtils, dataRedactor);
    }

    @Provides
    @Singleton
    public static SmsSecurityAgent provideSmsSecurityAgent(AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils, DataRedactor dataRedactor) {
        return new SmsSecurityAgent(appExecutors, tfLiteEngine, featureVectorizer, tensorUtils, dataRedactor);
    }

    @Provides
    @Singleton
    public static AppSecurityAgent provideAppSecurityAgent(@ApplicationContext Context context, AppExecutors appExecutors, TFLiteEngine tfLiteEngine, FeatureVectorizer featureVectorizer, TensorUtils tensorUtils, DataRedactor dataRedactor) {
        return new AppSecurityAgent(context, appExecutors, tfLiteEngine, featureVectorizer, tensorUtils, dataRedactor);
    }

    @Provides
    @Singleton
    public static AdaptiveAgentOrchestrator provideAdaptiveAgentOrchestrator(SystemResourceMonitor resourceMonitor, PhishingDetectorAgent phishingAgent, MalwareAnomalyAgent malwareAgent, NetworkGuardAgent networkAgent, CallIntelligenceAgent callAgent, PaymentProtectionAgent paymentAgent, SmsSecurityAgent smsAgent, AppSecurityAgent appAgent) {
        return new AdaptiveAgentOrchestrator(resourceMonitor, phishingAgent, malwareAgent, networkAgent, callAgent, paymentAgent, smsAgent, appAgent);
    }
}
