package com.cybershield.ai;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.data.service.SecurityGuardService;
import com.cybershield.ai.core.data.worker.PeriodicSecurityAuditWorker;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class CyberShieldApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startSecurityGuardService();
        schedulePeriodicAuditWorker();
    }

    private void startSecurityGuardService() {
        Intent serviceIntent = new Intent(this, SecurityGuardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void schedulePeriodicAuditWorker() {
        PeriodicWorkRequest auditRequest = new PeriodicWorkRequest.Builder(
                PeriodicSecurityAuditWorker.class,
                6, TimeUnit.HOURS
        ).addTag(AppConstants.WORK_TAG_PERIODIC_AUDIT).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AppConstants.WORK_TAG_PERIODIC_AUDIT,
                ExistingPeriodicWorkPolicy.KEEP,
                auditRequest
        );
    }
}
