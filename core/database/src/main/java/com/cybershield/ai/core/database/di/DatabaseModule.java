package com.cybershield.ai.core.database.di;

import android.content.Context;

import androidx.room.Room;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.database.AppDatabase;
import com.cybershield.ai.core.database.dao.SecurityScanDao;
import com.cybershield.ai.core.database.dao.ThreatEventDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                AppConstants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build();
    }

    @Provides
    @Singleton
    public static ThreatEventDao provideThreatEventDao(AppDatabase database) {
        return database.threatEventDao();
    }

    @Provides
    @Singleton
    public static SecurityScanDao provideSecurityScanDao(AppDatabase database) {
        return database.securityScanDao();
    }
}
