package com.cybershield.ai.core.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.feature.agents.orchestrator.AgentOrchestrator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Foreground Guard Service.
 * Runs persistent real-time AI security monitoring and listens to power events
 * to trigger dynamic agent orchestration.
 */
@AndroidEntryPoint
public class SecurityGuardService extends Service {

    @Inject
    AgentOrchestrator agentOrchestrator;

    private BroadcastReceiver powerReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(AppConstants.NOTIFICATION_ID_SECURITY_GUARD, createNotification());

        registerPowerStateReceiver();
        if (agentOrchestrator != null) {
            agentOrchestrator.updateOrchestrationPolicy(this);
        }
    }

    private void registerPowerStateReceiver() {
        powerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (agentOrchestrator != null) {
                    agentOrchestrator.updateOrchestrationPolicy(context);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (powerReceiver != null) {
            unregisterReceiver(powerReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppConstants.CHANNEL_ID_SECURITY_SERVICE,
                    "CyberShield AI Active Guard",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Continuous real-time AI threat monitoring service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID_SECURITY_SERVICE)
                .setContentTitle("CyberShield AI Guard Active")
                .setContentText("On-device Multi-Agent AI system is actively protecting your device")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
