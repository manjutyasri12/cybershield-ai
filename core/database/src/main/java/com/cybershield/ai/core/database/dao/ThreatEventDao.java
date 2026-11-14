package com.cybershield.ai.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cybershield.ai.core.database.entity.ThreatEvent;

import java.util.List;

@Dao
public interface ThreatEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertThreat(ThreatEvent threatEvent);

    @Query("SELECT * FROM threat_events ORDER BY timestamp DESC")
    LiveData<List<ThreatEvent>> getAllThreatsLiveData();

    @Query("SELECT * FROM threat_events ORDER BY timestamp DESC")
    List<ThreatEvent> getAllThreatsSync();

    @Query("SELECT * FROM threat_events WHERE isMitigated = 0 ORDER BY riskScore DESC")
    LiveData<List<ThreatEvent>> getUnmitigatedThreatsLiveData();

    @Query("SELECT COUNT(*) FROM threat_events WHERE isMitigated = 0")
    int getActiveThreatCountSync();

    @Update
    void updateThreat(ThreatEvent threatEvent);

    @Query("DELETE FROM threat_events WHERE id = :id")
    void deleteThreatById(long id);

    @Query("DELETE FROM threat_events")
    void clearAllThreats();
}
