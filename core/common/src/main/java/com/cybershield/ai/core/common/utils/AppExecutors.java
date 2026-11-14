package com.cybershield.ai.core.common.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Global executor pools for the application.
 * Manages background thread pools for Disk I/O, Network I/O, AI Inference,
 * and Main thread dispatching without blocking the UI thread.
 */
@Singleton
public class AppExecutors {

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor aiExecutor;
    private final Executor mainThread;

    public AppExecutors(Executor diskIO, Executor networkIO, Executor aiExecutor, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.aiExecutor = aiExecutor;
        this.mainThread = mainThread;
    }

    @Inject
    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(),
             Executors.newFixedThreadPool(3),
             Executors.newFixedThreadPool(2),
             new MainThreadExecutor());
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor aiExecutor() {
        return aiExecutor;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
