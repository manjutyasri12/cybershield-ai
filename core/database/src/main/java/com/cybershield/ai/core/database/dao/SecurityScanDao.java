package com.cybershield.ai.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cybershield.ai.core.database.entity.SecurityScan;

import java.util.List;

@Dao
public interface SecurityScanDao {

    @Insert
    long insertScan(SecurityScan scan);

    @Query("SELECT * FROM security_scans ORDER BY timestamp DESC")
    LiveData<List<SecurityScan>> getAllScansLiveData();

    @Query("SELECT * FROM security_scans ORDER BY timestamp DESC LIMIT 1")
    LiveData<SecurityScan> getLatestScanLiveData();

    @Query("SELECT * FROM security_scans ORDER BY timestamp DESC LIMIT 1")
    SecurityScan getLatestScanSync();

    @Query("DELETE FROM security_scans")
    void clearAllScans();
}
