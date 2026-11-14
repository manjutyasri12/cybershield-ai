package com.cybershield.ai.core.network.auth;

import com.cybershield.ai.core.common.result.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Firebase Authentication & Optional Cloud Sync manager.
 * Respects Privacy-First principles: auth handles user identity only;
 * threat logs stay on-device unless cloud backup is explicitly enabled.
 */
@Singleton
public class FirebaseAuthManager {

    private final FirebaseAuth firebaseAuth;

    @Inject
    public FirebaseAuthManager() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        callback.onResult(Resource.success(authResult.getUser()));
                    } else {
                        callback.onResult(Resource.error("Authentication failed: empty user", null));
                    }
                })
                .addOnFailureListener(e -> callback.onResult(Resource.error(e.getMessage() != null ? e.getMessage() : "Authentication error", e)));
    }

    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        callback.onResult(Resource.success(authResult.getUser()));
                    } else {
                        callback.onResult(Resource.error("User creation failed", null));
                    }
                })
                .addOnFailureListener(e -> callback.onResult(Resource.error(e.getMessage() != null ? e.getMessage() : "Registration error", e)));
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public interface AuthCallback {
        void onResult(Resource<FirebaseUser> result);
    }
}
