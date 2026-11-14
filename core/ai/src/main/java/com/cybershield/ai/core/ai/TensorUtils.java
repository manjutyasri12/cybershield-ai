package com.cybershield.ai.core.ai;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mathematical utilities for Tensor manipulation, Softmax normalization,
 * and classification confidence mapping.
 */
@Singleton
public class TensorUtils {

    @Inject
    public TensorUtils() {
    }

    public float[] softmax(float[] logits) {
        float[] probabilities = new float[logits.length];
        float max = Float.NEGATIVE_INFINITY;
        for (float val : logits) {
            if (val > max) max = val;
        }

        float sum = 0.0f;
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - max);
            sum += probabilities[i];
        }

        if (sum > 0) {
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= sum;
            }
        }
        return probabilities;
    }

    public int argmax(float[] probabilities) {
        int maxIndex = 0;
        float maxValue = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxValue) {
                maxValue = probabilities[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
