package com.cybershield.ai.core.security.di;

import android.content.Context;
import com.cybershield.ai.core.security.CryptoManager;
import com.cybershield.ai.core.security.DataRedactor;
import com.cybershield.ai.core.security.SecurityPreferences;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class SecurityModule {

    @Provides
    @Singleton
    public static CryptoManager provideCryptoManager() {
        return new CryptoManager();
    }

    @Provides
    @Singleton
    public static SecurityPreferences provideSecurityPreferences(@ApplicationContext Context context) {
        return new SecurityPreferences(context);
    }

    @Provides
    @Singleton
    public static DataRedactor provideDataRedactor() {
        return new DataRedactor();
    }
}
