package com.cybershield.ai.core.network.api;

import com.cybershield.ai.core.network.model.FogPayload;
import com.cybershield.ai.core.network.model.FogResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit REST API interface for Fog Node offloading and threat intelligence consensus.
 */
public interface FogNodeApi {

    @POST("threat/verify")
    Call<FogResponse> verifyThreat(@Body FogPayload payload);
}
