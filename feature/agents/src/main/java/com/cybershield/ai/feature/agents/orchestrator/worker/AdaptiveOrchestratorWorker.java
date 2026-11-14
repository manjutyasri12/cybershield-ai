package com.cybershield.ai.feature.agents.orchestrator.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cybershield.ai.feature.agents.orchestrator.AdaptiveAgentOrchestrator;

import javax.inject.Inject;

/**
 * WorkManager worker executing periodic battery-friendly resource evaluation
 * and agent state updates.
 */
public class AdaptiveOrchestratorWorker extends Worker {

    @Inject
    AdaptiveAgentOrchestrator adaptiveAgentOrchestrator;

    public AdaptiveOrchestratorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (adaptiveAgentOrchestrator != null) {
            adaptiveAgentOrchestrator.evaluateAndDispatch();
            return Result.success();
        }
        return Result.retry();
    }
}
