package com.cybershield.ai.core.common.constants;

public final class AppConstants {

    private AppConstants() {
        // Enforce utility class non-instantiability
    }

    // Database
    public static final String DATABASE_NAME = "cybershield_security.db";
    public static final int DATABASE_VERSION = 1;

    // Security & Encryption
    public static final String MASTER_KEY_ALIAS = "_cybershield_master_key_";
    public static final String PREF_FILE_NAME = "cybershield_encrypted_prefs";

    // Dynamic AI Agent Thresholds & Power Management
    public static final float THREAT_THRESHOLD_HIGH = 0.85f;
    public static final float THREAT_THRESHOLD_MEDIUM = 0.60f;
    public static final float THREAT_THRESHOLD_LOW = 0.35f;

    public static final int BATTERY_CRITICAL_LEVEL = 15; // Percent
    public static final int BATTERY_LOW_LEVEL = 30; // Percent

    // Agent Identifiers
    public static final String AGENT_PHISHING = "AGENT_PHISHING_DETECTOR";
    public static final String AGENT_MALWARE = "AGENT_MALWARE_ANOMALY";
    public static final String AGENT_NETWORK = "AGENT_NETWORK_GUARD";
    public static final String AGENT_CALL_INTELLIGENCE = "AGENT_CALL_INTELLIGENCE";
    public static final String AGENT_PAYMENT_PROTECTION = "AGENT_PAYMENT_PROTECTION";
    public static final String AGENT_SMS_SECURITY = "AGENT_SMS_SECURITY";

    // Fog Computing API Endpoints & Timeouts
    public static final String FOG_NODE_DEFAULT_URL = "https://fog.cybershield.local/api/v1/";
    public static final long NETWORK_TIMEOUT_SECONDS = 15;

    // WorkManager Task Tags
    public static final String WORK_TAG_PERIODIC_AUDIT = "work_periodic_security_audit";
    public static final String WORK_TAG_FOG_SYNC = "work_fog_cloud_sync";

    // Foreground Service
    public static final int NOTIFICATION_ID_SECURITY_GUARD = 1001;
    public static final String CHANNEL_ID_SECURITY_SERVICE = "channel_cybershield_guard";
}
