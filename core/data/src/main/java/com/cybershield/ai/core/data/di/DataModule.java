package com.cybershield.ai.core.data.di;

import com.cybershield.ai.core.data.repository.SecurityRepository;
import com.cybershield.ai.core.data.repository.SecurityRepositoryImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class DataModule {

    @Binds
    @Singleton
    public abstract SecurityRepository bindSecurityRepository(SecurityRepositoryImpl repositoryImpl);
}
