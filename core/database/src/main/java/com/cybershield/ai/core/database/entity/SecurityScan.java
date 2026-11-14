package com.cybershield.ai.core.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity recording historical security audit scans.
 */
@Entity(tableName = "security_scans")
public class SecurityScan {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long timestamp;
    private int itemsScanned;
    private int threatsFound;
    private String scanStatus; // CLEAN, WARNING, THREAT_DETECTED
    private long durationMs;

    public SecurityScan(long timestamp, int itemsScanned, int threatsFound, String scanStatus, long durationMs) {
        this.timestamp = timestamp;
        this.itemsScanned = itemsScanned;
        this.threatsFound = threatsFound;
        this.scanStatus = scanStatus;
        this.durationMs = durationMs;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getItemsScanned() { return itemsScanned; }
    public void setItemsScanned(int itemsScanned) { this.itemsScanned = itemsScanned; }

    public int getThreatsFound() { return threatsFound; }
    public void setThreatsFound(int threatsFound) { this.threatsFound = threatsFound; }

    public String getScanStatus() { return scanStatus; }
    public void setScanStatus(String scanStatus) { this.scanStatus = scanStatus; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
