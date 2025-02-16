package me.croabeast.lib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

class StringSimpleApplier extends SimpleApplier<String> implements StringApplier {

    StringSimpleApplier(String object) {
        super(object);
    }

    @NotNull
    public StringSimpleApplier apply(ApplierPriority priority, UnaryOperator<String> operator) {
        return apply(operator);
    }

    @NotNull
    public StringSimpleApplier apply(UnaryOperator<String> operator) {
        super.apply(operator);
        return this;
    }

    @Override
    public String toString() {
        return result();
    }
}
