package com.cybershield.ai.feature.agents.orchestrator.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.cybershield.ai.feature.agents.orchestrator.model.SystemMetrics;

import java.io.RandomAccessFile;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System Resource Monitor evaluating CPU, RAM, Battery, Charging State,
 * Network Status, User Activity, and Foreground App without high battery consumption.
 */
@Singleton
public class SystemResourceMonitor {

    private final Context context;

    @Inject
    public SystemResourceMonitor(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public SystemMetrics captureMetrics() {
        float cpuUsage = sampleCpuUsage();
        float ramUsage = sampleRamUsage();
        int batteryLevel = sampleBatteryLevel();
        boolean isCharging = sampleChargingState();
        boolean isNetworkAvailable = sampleNetworkAvailable();
        boolean isWifi = sampleWifiConnected();
        String foregroundApp = sampleForegroundApp();
        boolean isUserActive = sampleUserActive();

        return new SystemMetrics(
                cpuUsage,
                ramUsage,
                batteryLevel,
                isCharging,
                isNetworkAvailable,
                isWifi,
                foregroundApp,
                isUserActive
        );
    }

    private float sampleCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();

            if (load != null && load.startsWith("cpu")) {
                String[] toks = load.split("\\s+");
                long idle = Long.parseLong(toks[4]);
                long total = 0;
                for (int i = 1; i < toks.length && i < 8; i++) {
                    total += Long.parseLong(toks[i]);
                }
                if (total > 0) {
                    long active = total - idle;
                    return (float) active / total * 100.0f;
                }
            }
        } catch (Exception ignored) {
        }
        return 20.0f; // Baseline fallback estimate
    }

    private float sampleRamUsage() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            long totalMem = memoryInfo.totalMem;
            long availMem = memoryInfo.availMem;
            if (totalMem > 0) {
                long usedMem = totalMem - availMem;
                return (float) usedMem / totalMem * 100.0f;
            }
        }
        return 40.0f;
    }

    private int sampleBatteryLevel() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, filter);
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                return (int) ((level / (float) scale) * 100.0f);
            }
        }
        return 80;
    }

    private boolean sampleChargingState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, filter);
        if (intent != null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        }
        return false;
    }

    private boolean sampleNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
            }
        }
        return false;
    }

    private boolean sampleWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    private String sampleForegroundApp() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null && activityManager.getRunningAppProcesses() != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return processInfo.processName;
                }
            }
        }
        return context.getPackageName();
    }

    private boolean sampleUserActive() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null && activityManager.getRunningAppProcesses() != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }
}
