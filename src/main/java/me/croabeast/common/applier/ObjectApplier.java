package me.croabeast.common.applier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents an object that can apply multiple operators to a single object.
 *
 * <p> Each operator is applied in the order that they were called, if there is no
 * defined priority to those operators.
 *
 * @param <T> object type
 */
public interface ObjectApplier<T> {

    /**
     * Applies the operator with the defined priority if the applier allows
     * prioritized operators.
     *
     * <p> If prioritized operators are not allowed, the operator will apply
     * directly to the current string.
     *
     * @param priority the priority
     * @param operator the operator
     *
     * @throws NullPointerException if the operator is null
     * @return a reference of this applier
     */
    @NotNull
    ObjectApplier<T> apply(ApplierPriority priority, UnaryOperator<T> operator);

    /**
     * Applies the operator directly to the current object if the applier does
     * not allow prioritized operators.
     *
     * <p> If prioritized operators are allowed, it will use {@link ApplierPriority#NORMAL}.
     *
     * @param operator the operator
     *
     * @throws NullPointerException if the operator is null
     * @return a reference of this applier
     */
    @NotNull
    ObjectApplier<T> apply(UnaryOperator<T> operator);

    /**
     * Returns the object after all the operators were applied.
     *
     * @return the applied string
     */
    T result();

    /**
     * Creates a new applier without prioritization in all its operators.
     *
     * <p> Each call of the {@link #apply(ApplierPriority, UnaryOperator)} and {@link #apply(UnaryOperator)}
     * methods, the operators are applied  directly to the object without any priority.
     *
     * @param object an object
     * @param <T> object type
     *
     * @throws NullPointerException if the object is null
     * @return a new non-prioritized applier
     */
    static <T> ObjectApplier<T> simplified(T object) {
        return new SimpleApplier<>(object);
    }

    /**
     * Creates a new applier without prioritization in all its operators.
     *
     * <p> Each call of the {@link #apply(ApplierPriority, UnaryOperator)} and {@link #apply(UnaryOperator)}
     * methods, the operators are applied  directly to the object without any priority.
     *
     * @param applier an applier
     * @param <T> object type
     *
     * @throws NullPointerException if the applier is null
     * @return a new non-prioritized applier
     */
    static <T> ObjectApplier<T> simplified(ObjectApplier<T> applier) {
        return simplified(Objects.requireNonNull(applier).result());
    }

    /**
     * Creates a new applier with prioritization for each operator that is being
     * added to the applier.
     *
     * <p> The {@link #apply(ApplierPriority, UnaryOperator)} method add the operator with the
     * defined priority, and the {@link #apply(UnaryOperator)} method add the operator
     * with the {@link ApplierPriority#NORMAL} priority.
     *
     * @param object an object
     * @param <T> object type
     *
     * @throws NullPointerException if the object is null
     * @return a new prioritized applier
     */
    static <T> ObjectApplier<T> prioritized(T object) {
        return new PriorityApplier<>(object);
    }

    /**
     * Creates a new applier with prioritization for each operator that is being
     * added to the applier.
     *
     * <p> The {@link #apply(ApplierPriority, UnaryOperator)} method add the operator with the
     * defined priority, and the {@link #apply(UnaryOperator)} method add the operator
     * with the {@link ApplierPriority#NORMAL} priority.
     *
     * @param applier an applier
     * @param <T> object type
     *
     * @throws NullPointerException if the applier is null
     * @return a new prioritized applier
     */
    static <T> ObjectApplier<T> prioritized(ObjectApplier<T> applier) {
        return prioritized(Objects.requireNonNull(applier).result());
    }
}
