package com.cybershield.ai.feature.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cybershield.ai.core.common.base.BaseViewModel;
import com.cybershield.ai.core.common.result.Resource;
import com.cybershield.ai.core.common.utils.AppExecutors;
import com.cybershield.ai.core.network.auth.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends BaseViewModel {

    private final FirebaseAuthManager authManager;
    private final MutableLiveData<Resource<FirebaseUser>> authResult = new MutableLiveData<>();

    @Inject
    public LoginViewModel(AppExecutors appExecutors, FirebaseAuthManager authManager) {
        super(appExecutors);
        this.authManager = authManager;
    }

    public LiveData<Resource<FirebaseUser>> getAuthResult() {
        return authResult;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Please enter email and password");
            return;
        }
        isLoading.setValue(true);
        authManager.signInWithEmail(email, password, result -> {
            isLoading.setValue(false);
            authResult.setValue(result);
        });
    }

    public void register(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Please enter email and password");
            return;
        }
        isLoading.setValue(true);
        authManager.signUpWithEmail(email, password, result -> {
            isLoading.setValue(false);
            authResult.setValue(result);
        });
    }
}
