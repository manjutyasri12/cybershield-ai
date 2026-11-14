package com.cybershield.ai.core.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.cybershield.ai.core.common.constants.AppConstants;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper for EncryptedSharedPreferences to handle secure preference persistence.
 */
@Singleton
public class SecurityPreferences {

    private final SharedPreferences encryptedPrefs;

    @Inject
    public SecurityPreferences(@NonNull Context context) {
        SharedPreferences prefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    AppConstants.PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            prefs = context.getSharedPreferences(AppConstants.PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
        this.encryptedPrefs = prefs;
    }

    public void putString(String key, String value) {
        encryptedPrefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return encryptedPrefs.getString(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        encryptedPrefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return encryptedPrefs.getBoolean(key, defaultValue);
    }

    public void putInt(String key, int value) {
        encryptedPrefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return encryptedPrefs.getInt(key, defaultValue);
    }

    public void remove(String key) {
        encryptedPrefs.edit().remove(key).apply();
    }
}
