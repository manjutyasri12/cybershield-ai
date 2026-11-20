package com.cybershield.ai.core.ai.di;

import com.cybershield.ai.core.ai.ExplainableRiskEngine;
import com.cybershield.ai.core.ai.FeatureVectorizer;
import com.cybershield.ai.core.ai.TFLiteEngine;
import com.cybershield.ai.core.ai.TensorUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class AiModule {

    @Provides
    @Singleton
    public static TFLiteEngine provideTFLiteEngine() {
        return new TFLiteEngine();
    }

    @Provides
    @Singleton
    public static FeatureVectorizer provideFeatureVectorizer() {
        return new FeatureVectorizer();
    }

    @Provides
    @Singleton
    public static TensorUtils provideTensorUtils() {
        return new TensorUtils();
    }

    @Provides
    @Singleton
    public static ExplainableRiskEngine provideExplainableRiskEngine() {
        return new ExplainableRiskEngine();
    }
}
