package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

class SimpleApplier<T> implements ObjectApplier<T> {

    private T object;

    SimpleApplier(T object) {
        this.object = Objects.requireNonNull(object);
    }

    @NotNull
    public SimpleApplier<T> apply(ApplierPriority priority, UnaryOperator<T> operator) {
        return apply(operator);
    }

    @NotNull
    public SimpleApplier<T> apply(UnaryOperator<T> operator) {
        object = Objects.requireNonNull(operator).apply(object);
        return this;
    }

    @Override
    public T result() {
        return object;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
