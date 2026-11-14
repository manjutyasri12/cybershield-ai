package com.cybershield.ai.core.data.worker;

import android.content.Context;

import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cybershield.ai.core.data.repository.SecurityRepository;

import javax.inject.Inject;

/**
 * WorkManager worker for executing periodic background security audits.
 */
public class PeriodicSecurityAuditWorker extends Worker {

    @Inject
    SecurityRepository securityRepository;

    public PeriodicSecurityAuditWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (securityRepository != null) {
            securityRepository.executeFullSecurityAudit(result -> {
                // Audit completed
            });
            return Result.success();
        }
        return Result.retry();
    }
}
