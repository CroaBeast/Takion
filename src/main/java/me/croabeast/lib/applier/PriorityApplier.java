package me.croabeast.lib.applier;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

class PriorityApplier<T> implements ObjectApplier<T> {

    private final Map<ApplierPriority, Set<UnaryOperator<T>>> os = new TreeMap<>(Comparator.reverseOrder());
    private final T object;

    PriorityApplier(T object) {
        this.object = Objects.requireNonNull(object);
    }

    @NotNull
    public PriorityApplier<T> apply(ApplierPriority priority, UnaryOperator<T> operator) {
        priority = priority == null ? ApplierPriority.NORMAL : priority;
        Objects.requireNonNull(operator);

        Set<UnaryOperator<T>> set = os.getOrDefault(priority, new LinkedHashSet<>());
        set.add(operator);

        os.put(priority, set);
        return this;
    }

    @NotNull
    public PriorityApplier<T> apply(UnaryOperator<T> operator) {
        return apply(null, operator);
    }

    @Override
    public T result() {
        SimpleApplier<T> applier = new SimpleApplier<>(object);
        os.forEach((p, o) -> o.forEach(applier::apply));
        return applier.result();
    }

    @Override
    public String toString() {
        return result().toString();
    }
}
