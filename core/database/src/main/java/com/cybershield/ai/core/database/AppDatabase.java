package com.cybershield.ai.core.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.cybershield.ai.core.common.constants.AppConstants;
import com.cybershield.ai.core.database.converter.DatabaseConverters;
import com.cybershield.ai.core.database.dao.SecurityScanDao;
import com.cybershield.ai.core.database.dao.ThreatEventDao;
import com.cybershield.ai.core.database.entity.SecurityScan;
import com.cybershield.ai.core.database.entity.ThreatEvent;

@Database(entities = {ThreatEvent.class, SecurityScan.class}, version = AppConstants.DATABASE_VERSION, exportSchema = false)
@TypeConverters({DatabaseConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ThreatEventDao threatEventDao();
    public abstract SecurityScanDao securityScanDao();
}
