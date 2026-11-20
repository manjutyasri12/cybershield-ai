package com.cybershield.ai.feature.agents.call_intelligence.service;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;

import androidx.annotation.RequiresApi;

import com.cybershield.ai.feature.agents.call_intelligence.CallIntelligenceAgent;
import com.cybershield.ai.feature.agents.call_intelligence.model.CallRecommendation;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Android Telecom CallScreeningService Integration.
 * Intercepts incoming calls at OS level, queries CallIntelligenceAgent,
 * and issues automated blocking, silencing, or user warning actions.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
@AndroidEntryPoint
public class CyberShieldCallScreeningService extends CallScreeningService {

    @Inject
    CallIntelligenceAgent callIntelligenceAgent;

    @SuppressLint("NewApi")
    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (callDetails == null || callDetails.getHandle() == null) {
            return;
        }

        Uri handle = callDetails.getHandle();
        String phoneNumber = handle.getSchemeSpecificPart();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            respondToCall(callDetails, new CallResponse.Builder().build());
            return;
        }
        boolean isContact = callDetails.getContactDisplayName() != null;

        if (callIntelligenceAgent != null) {
            callIntelligenceAgent.analyzeIncomingCall(phoneNumber, isContact, "", result -> {
                CallResponse.Builder responseBuilder = new CallResponse.Builder();

                if (result.getRecommendation() == CallRecommendation.BLOCK_CALL) {
                    responseBuilder.setDisallowCall(true);
                    responseBuilder.setRejectCall(true);
                    responseBuilder.setSkipCallLog(false);
                    responseBuilder.setSkipNotification(false);
                } else if (result.getRecommendation() == CallRecommendation.SILENCE_CALL) {
                    responseBuilder.setSilenceCall(true);
                }

                respondToCall(callDetails, responseBuilder.build());
            });
        } else {
            respondToCall(callDetails, new CallResponse.Builder().build());
        }
    }
}
