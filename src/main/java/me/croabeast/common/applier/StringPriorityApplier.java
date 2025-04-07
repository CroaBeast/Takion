package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

class StringPriorityApplier extends PriorityApplier<String> implements StringApplier {

    StringPriorityApplier(String object) {
        super(object);
    }

    @NotNull
    public StringPriorityApplier apply(ApplierPriority priority, UnaryOperator<String> operator) {
        super.apply(priority, operator);
        return this;
    }

    @NotNull
    public StringPriorityApplier apply(UnaryOperator<String> operator) {
        return apply(null, operator);
    }

    @Override
    public String toString() {
        return result();
    }
}
