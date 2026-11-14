package com.cybershield.ai.core.ai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

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

    private Interpreter interpreter;

    @Inject
    public TFLiteEngine() {
    }

    public synchronized boolean loadModel(@NonNull Context context, @NonNull String modelAssetPath) {
        try {
            MappedByteBuffer modelBuffer = loadModelFile(context, modelAssetPath);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(2); // Optimization for battery & thermal efficiency
            interpreter = new Interpreter(modelBuffer, options);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    private MappedByteBuffer loadModelFile(@NonNull Context context, @NonNull String modelAssetPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelAssetPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public synchronized float[][] runInference(float[][] inputVector, int outputClasses) {
        float[][] output = new float[1][outputClasses];
        if (interpreter != null) {
            interpreter.run(inputVector, output);
        } else {
            // Fallback default response if model asset is uninitialized
            for (int i = 0; i < outputClasses; i++) {
                output[0][i] = 0.0f;
            }
        }
        return output;
    }

    public synchronized void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
