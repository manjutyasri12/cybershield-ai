package com.cybershield.ai.core.common.result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic wrapper class for conveying state (Success, Error, Loading)
 * across data and presentation layers.
 */
public class Resource<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    @Nullable
    public final Throwable throwable;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message, @Nullable Throwable throwable) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.throwable = throwable;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg, null);
    }

    public static <T> Resource<T> error(@NonNull String msg, @Nullable Throwable t) {
        return new Resource<>(Status.ERROR, null, msg, t);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null, null);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
