package com.cybershield.ai.core.common.di;

import com.cybershield.ai.core.common.utils.AppExecutors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class CommonModule {

    @Provides
    @Singleton
    public static AppExecutors provideAppExecutors() {
        return new AppExecutors();
    }
}
