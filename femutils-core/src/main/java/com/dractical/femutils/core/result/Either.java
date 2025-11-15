package com.dractical.femutils.core.result;

import com.dractical.femutils.core.check.Checks;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Either type. Exactly one of left or right is present. Normally:
 * - Left = error / alternative / "bad"
 * - Right = value / primary / "good"
 */
@SuppressWarnings("unused")
public final class Either<L, R> {
    private final L left;
    private final R right;
    private final boolean rightPresent;

    private Either(L left, R right, boolean rightPresent) {
        this.left = left;
        this.right = right;
        this.rightPresent = rightPresent;
    }

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(Checks.notNull(value, "left"), null, false);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(null, Checks.notNull(value, "right"), true);
    }

    public boolean isLeft() {
        return !rightPresent;
    }

    public boolean isRight() {
        return rightPresent;
    }

    public L leftOrNull() {
        return left;
    }

    public R rightOrNull() {
        return right;
    }

    public L getLeft() {
        if (!rightPresent) {
            return left;
        }
        throw new IllegalStateException("Either is right, not left");
    }

    public R getRight() {
        if (rightPresent) {
            return right;
        }
        throw new IllegalStateException("Either is left, not right");
    }

    public <NL> Either<NL, R> mapLeft(Function<? super L, ? extends NL> mapper) {
        if (mapper == null || rightPresent) {
            //noinspection unchecked
            return (Either<NL, R>) this;
        }
        return left(mapper.apply(left));
    }

    public <NR> Either<L, NR> mapRight(Function<? super R, ? extends NR> mapper) {
        if (mapper == null || !rightPresent) {
            //noinspection unchecked
            return (Either<L, NR>) this;
        }
        return right(mapper.apply(right));
    }

    public <T> T fold(Function<? super L, ? extends T> leftFn, Function<? super R, ? extends T> rightFn) {
        Checks.notNull(leftFn, "leftFn");
        Checks.notNull(rightFn, "rightFn");
        return rightPresent ? rightFn.apply(right) : leftFn.apply(left);
    }

    public Either<L, R> ifLeft(Consumer<? super L> consumer) {
        if (!rightPresent && consumer != null) {
            consumer.accept(left);
        }
        return this;
    }

    public Either<L, R> ifRight(Consumer<? super R> consumer) {
        if (rightPresent && consumer != null) {
            consumer.accept(right);
        }
        return this;
    }

    @Override
    public String toString() {
        return rightPresent ? "Either.right(" + right + ")" : "Either.left(" + left + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Either<?, ?> other)) return false;
        return rightPresent == other.rightPresent && Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(rightPresent);
        result = 31 * result + Objects.hashCode(left);
        result = 31 * result + Objects.hashCode(right);
        return result;
    }
}
