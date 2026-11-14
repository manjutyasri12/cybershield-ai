package com.cybershield.ai.core.common.base;

import com.cybershield.ai.core.common.result.Resource;
import com.cybershield.ai.core.common.utils.AppExecutors;

/**
 * Base abstract repository implementing offline-first caching logic
 * and thread execution coordination.
 */
public abstract class BaseRepository {

    protected final AppExecutors appExecutors;

    public BaseRepository(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public interface Callback<T> {
        void onResult(Resource<T> result);
    }
}
