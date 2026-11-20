package com.cybershield.ai.feature.agents.orchestrator.model;

import java.util.Locale;

public class SystemMetrics {

    private final float cpuUsagePercent;
    private final float ramUsagePercent;
    private final int batteryLevelPercent;
    private final boolean isCharging;
    private final boolean isNetworkAvailable;
    private final boolean isWifiConnected;
    private final String foregroundAppName;
    private final boolean isUserActive;

    public SystemMetrics(
            float cpuUsagePercent,
            float ramUsagePercent,
            int batteryLevelPercent,
            boolean isCharging,
            boolean isNetworkAvailable,
            boolean isWifiConnected,
            String foregroundAppName,
            boolean isUserActive
    ) {
        this.cpuUsagePercent = cpuUsagePercent;
        this.ramUsagePercent = ramUsagePercent;
        this.batteryLevelPercent = batteryLevelPercent;
        this.isCharging = isCharging;
        this.isNetworkAvailable = isNetworkAvailable;
        this.isWifiConnected = isWifiConnected;
        this.foregroundAppName = foregroundAppName;
        this.isUserActive = isUserActive;
    }

    public float getCpuUsagePercent() { return cpuUsagePercent; }
    public float getRamUsagePercent() { return ramUsagePercent; }
    public int getBatteryLevelPercent() { return batteryLevelPercent; }
    public boolean isCharging() { return isCharging; }
    public boolean isNetworkAvailable() { return isNetworkAvailable; }
    public boolean isWifiConnected() { return isWifiConnected; }
    public String getForegroundAppName() { return foregroundAppName; }
    public boolean isUserActive() { return isUserActive; }

    @Override
    public String toString() {
        return "SystemMetrics{" +
                "CPU=" + String.format(Locale.ROOT, "%.1f", cpuUsagePercent) + "%" +
                ", RAM=" + String.format(Locale.ROOT, "%.1f", ramUsagePercent) + "%" +
                ", Battery=" + batteryLevelPercent + "%" +
                ", Charging=" + isCharging +
                ", Network=" + isNetworkAvailable +
                ", App='" + foregroundAppName + '\'' +
                '}';
    }
}
