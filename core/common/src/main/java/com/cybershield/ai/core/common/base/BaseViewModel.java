package com.cybershield.ai.core.common.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cybershield.ai.core.common.utils.AppExecutors;

/**
 * Abstract BaseViewModel providing thread-safe execution and uniform error handling
 * across MVVM presentation components.
 */
public abstract class BaseViewModel extends ViewModel {

    protected final AppExecutors appExecutors;
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public BaseViewModel(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    protected void postError(String message) {
        errorMessage.postValue(message);
    }

    protected void postLoading(boolean loading) {
        isLoading.postValue(loading);
    }
}
