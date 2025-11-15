package com.dractical.femutils.core.result;

import com.dractical.femutils.core.check.Checks;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Container for an operation result.
 */
@SuppressWarnings("unused")
public final class Result<T> {

    private final T value;
    private final Throwable error;

    private Result(T value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful Result.
     */
    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null);
    }

    /**
     * Creates a failure Result with the given error.
     */
    public static <T> Result<T> error(Throwable error) {
        Checks.notNull(error, "error");
        return new Result<>(null, error);
    }

    /**
     * Executes a task and captures any thrown exception as a failure Result.
     */
    public static <T> Result<T> of(ThrowingSupplier<T> supplier) {
        Checks.notNull(supplier, "supplier");
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return error(t);
        }
    }

    /**
     * @return true if this is a successful result.
     */
    public boolean isOk() {
        return error == null;
    }

    /**
     * @return true if this is a failure result.
     */
    public boolean isError() {
        return error != null;
    }

    /**
     * @return the successful value, or null if this is an error.
     */
    public T orNull() {
        return value;
    }

    /**
     * @return the error if present, otherwise null.
     */
    public Throwable errorOrNull() {
        return error;
    }

    /**
     * @return the value if ok, otherwise the fallback.
     */
    public T orElse(T fallback) {
        return isOk() ? value : fallback;
    }

    /**
     * @return the value if ok, otherwise a lazily computed fallback.
     */
    public T orElseGet(Function<? super Throwable, ? extends T> fallback) {
        if (isOk() || fallback == null) {
            return value;
        }
        return fallback.apply(error);
    }

    /**
     * @return the value if ok, if error, throws that error.
     */
    public T getOrThrow() {
        if (isOk()) {
            return value;
        }
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        }
        if (error instanceof Error) {
            throw (Error) error;
        }
        throw new RuntimeException(error);
    }

    /**
     * Applies mapper only if this is ok, errors propagate unchanged.
     */
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        if (!isOk() || mapper == null) {
            //noinspection unchecked
            return (Result<U>) this;
        }
        try {
            return ok(mapper.apply(value));
        } catch (Throwable t) {
            return error(t);
        }
    }

    /**
     * Applies mapper that itself returns a Result, only if this is ok.
     */
    public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        if (!isOk() || mapper == null) {
            //noinspection unchecked
            return (Result<U>) this;
        }
        try {
            Result<U> next = mapper.apply(value);
            return next != null ? next : error(new NullPointerException("flatMap produced null Result"));
        } catch (Throwable t) {
            return error(t);
        }
    }

    /**
     * Transforms the error if this is a failure; ok values propagate unchanged.
     */
    public Result<T> mapError(Function<? super Throwable, ? extends Throwable> mapper) {
        if (isOk() || mapper == null) {
            return this;
        }
        Throwable mapped = mapper.apply(error);
        return error(mapped != null ? mapped : error);
    }

    /**
     * Run a side effect if this is ok.
     */
    public Result<T> ifOk(Consumer<? super T> consumer) {
        if (isOk() && consumer != null) {
            consumer.accept(value);
        }
        return this;
    }

    /**
     * Run a side effect if this is an error.
     */
    public Result<T> ifError(Consumer<? super Throwable> consumer) {
        if (isError() && consumer != null) {
            consumer.accept(error);
        }
        return this;
    }

    @Override
    public String toString() {
        return isOk() ? "Result.ok(" + value + ")" : "Result.error(" + error + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Result<?> other)) return false;
        return Objects.equals(value, other.value) && Objects.equals(error, other.error);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(value) + Objects.hashCode(error);
    }

    /**
     * Supplier that may throw a checked exception.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
