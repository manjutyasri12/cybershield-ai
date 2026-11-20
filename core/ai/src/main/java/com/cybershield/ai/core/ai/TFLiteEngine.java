package com.cybershield.ai.core.ai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Embedded TensorFlow Lite Inference Engine for On-Device Edge Computing.
 * Handles model loading, thread allocation, and execution delegates.
 */
@Singleton
public class TFLiteEngine {

    private static final String TAG = "TFLiteEngine";

    private Interpreter interpreter;

    @Inject
    public TFLiteEngine() {
    }

    public synchronized boolean loadModel(@NonNull Context context, @NonNull String modelAssetPath) {
        close();
        try {
            MappedByteBuffer modelBuffer = loadModelFile(context, modelAssetPath);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(2);
            interpreter = new Interpreter(modelBuffer, options);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Unable to load on-device model: " + modelAssetPath, e);
            return false;
        }
    }

    public synchronized boolean isModelLoaded() {
        return interpreter != null;
    }

    @NonNull
    private MappedByteBuffer loadModelFile(@NonNull Context context, @NonNull String modelAssetPath) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelAssetPath);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getDeclaredLength()
            );
        }
    }

    public synchronized float[][] runInference(float[][] inputVector, int outputClasses) {
        float[][] output = new float[1][outputClasses];
        if (interpreter == null) {
            // Other agents retain their explainable heuristic checks while no model is configured.
            output[0][0] = 10.0f;
            return output;
        }

        interpreter.run(inputVector, output);
        return output;
    }

    public synchronized void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
