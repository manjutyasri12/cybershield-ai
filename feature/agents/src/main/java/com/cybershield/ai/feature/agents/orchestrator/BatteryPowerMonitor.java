package com.cybershield.ai.feature.agents.orchestrator;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.NonNull;

import com.cybershield.ai.core.common.constants.AppConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Battery & Power Monitor for Dynamic AI Agent Scheduling.
 * Evaluates real-time battery level, charging state, and power saver mode.
 */
@Singleton
public class BatteryPowerMonitor {

    @Inject
    public BatteryPowerMonitor() {
    }

    public enum PowerPolicy {
        PERFORMANCE,  // Charging or >50% battery: All agents ACTIVE
        BALANCED,     // 30% - 50% battery: Critical agents ACTIVE, others PERIODIC
        SAVER         // <30% battery or Low Power Mode: Agents DORMANT or LOW_POWER_PERIODIC
    }

    @NonNull
    public PowerPolicy evaluatePowerPolicy(@NonNull Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        if (batteryStatus == null) {
            return PowerPolicy.BALANCED;
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            return PowerPolicy.PERFORMANCE;
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = (level / (float) scale) * 100.0f;

        if (batteryPct <= AppConstants.BATTERY_CRITICAL_LEVEL) {
            return PowerPolicy.SAVER;
        } else if (batteryPct <= AppConstants.BATTERY_LOW_LEVEL) {
            return PowerPolicy.SAVER;
        } else if (batteryPct > 60.0f) {
            return PowerPolicy.PERFORMANCE;
        } else {
            return PowerPolicy.BALANCED;
        }
    }
}
