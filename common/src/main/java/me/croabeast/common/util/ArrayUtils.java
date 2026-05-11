package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Utility methods for working with Java arrays and converting them to collections.
 * <p>
 * Provides operations such as combining multiple arrays, checking for emptiness,
 * validating array contents, converting arrays to lists or sets, and applying
 * transformations to array elements.
 * </p>
 */
@SuppressWarnings("unchecked")
@UtilityClass
public class ArrayUtils {

    /**
     * Checks whether the given array is null or contains no elements.
     *
     * @param  <T>   the component type of the array
     * @param  array the array to check
     * @return       {@code true} if {@code array} is null or has length zero; {@code false} otherwise
     */
    @SafeVarargs
    public <T> boolean isArrayEmpty(T... array) {
        return array == null || array.length < 1;
    }

    /**
     * Combines a base array with one or more additional arrays into a single array.
     * <p>
     * Null or empty extra arrays are ignored. The resulting array is of the same
     * component type as the base array.
     * </p>
     *
     * @param  <T>         the component type of the arrays
     * @param  array       the base array (must not be null)
     * @param  extraArrays zero or more additional arrays to append
     * @return             a new array containing all elements of {@code array} followed by all elements of each {@code extraArrays}
     */
    @SafeVarargs
    public <T> T[] combineArrays(@NotNull T[] array, T[]... extraArrays) {
        if (isArrayEmpty(extraArrays)) return array;

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : extraArrays)
            if (!isArrayEmpty(a)) Collections.addAll(resultList, a);

        Class<?> componentType = array.getClass().getComponentType();
        T[] result = (T[]) Array.newInstance(componentType, 0);
        return resultList.toArray(result);
    }

    /**
     * Validates that the array is not empty and returns it.
     * <p>
     * Throws an exception if the array is null or empty.
     * </p>
     *
     * @param  <T>   the component type of the array
     * @param  array the array to validate
     * @return       the same array if it is not empty
     * @throws IllegalArgumentException if the array is null or empty
     */
    @SafeVarargs
    public <T> T[] checkArray(T... array) {
        return Exceptions.validate(array, ArrayUtils::isArrayEmpty, "Array should be declared");
    }

    /**
     * Converts the given varargs array into the provided collection, optionally applying
     * a transformation function to each element.
     *
     * @param  <T>        the element type
     * @param  <I>        the type of the target collection
     * @param  collection the collection to populate (must not be null)
     * @param  function   an optional transformation to apply to each element (may be null)
     * @param  array      the source array of elements
     * @return            the populated collection
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, UnaryOperator<T> function, T... array) {
        Objects.requireNonNull(collection, "Target collection must not be null");

        if (isArrayEmpty(array)) return collection;

        for (T o : array) {
            o = function == null ? o : function.apply(o);
            collection.add(o);
        }

        return collection;
    }

    /**
     * Converts the given varargs array into the provided collection without transformations.
     *
     * @param  <T>        the element type
     * @param  <I>        the type of the target collection
     * @param  collection the collection to populate (must not be null)
     * @param  array      the source array of elements
     * @return            the populated collection
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, T... array) {
        return toCollection(collection, null, array);
    }

    /**
     * Converts the given array into a {@link List}, applying an optional transformation to each element.
     *
     * @param  <T>      the element type
     * @param  operator the transformation function to apply to each element (may be null)
     * @param  array    the source array
     * @return          a new {@link List} containing the (possibly transformed) elements
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> toList(UnaryOperator<T> operator, T... array) {
        return toCollection(new ArrayList<>(), operator, array);
    }

    /**
     * Converts the given array into a {@link List} without transformations.
     *
     * @param  <T>   the element type
     * @param  array the source array
     * @return       a new {@link List} containing the elements
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> toList(T... array) {
        return toCollection(new ArrayList<>(), array);
    }

    /**
     * Converts the given array into a {@link Set}.
     *
     * @param  <T>   the element type
     * @param  array the source array
     * @return       a new {@link Set} containing the elements
     */
    @SafeVarargs
    @NotNull
    public <T> Set<T> toSet(T... array) {
        return toCollection(new HashSet<>(), array);
    }

    /**
     * Applies a sequence of {@link UnaryOperator} transformations to each element of the array in place.
     *
     * @param  <T>       the element type
     * @param  array     the array whose elements will be transformed (must not be null)
     * @param  operators the operators to apply in order to each element
     * @return           the same array instance with transformed elements
     */
    @SafeVarargs
    @NotNull
    public <T> T[] applyToArray(T[] array, UnaryOperator<T>... operators) {
        Objects.requireNonNull(array, "Array must not be null");

        for (int i = 0; i < array.length; i++) {
            T temp = array[i];
            for (UnaryOperator<T> o : toList(operators))
                temp = o.apply(temp);
            array[i] = temp;
        }

        return array;
    }

    /**
     * Applies a mapping function to each element of the source array and returns a new array of the results.
     *
     * @param  <T>      the source element type
     * @param  <U>      the target element type
     * @param  array    the source array (must not be null)
     * @param  function the mapping function to apply to each element (must not be null)
     * @return          a new array containing the mapped elements
     */
    @NotNull
    public <T, U> U[] applyToArray(T[] array, Function<T, U> function) {
        Objects.requireNonNull(array, "Array must not be null");
        Objects.requireNonNull(function, "Function must not be null");

        List<U> result = new ArrayList<>();
        for (T t : array)
            result.add(function.apply(t));

        return (U[]) result.toArray();
    }

    /**
     * Checks whether the given array contains the specified element.
     *
     * @param  <T>    the element type
     * @param  array  the array to search
     * @param  object the element to look for
     * @return        {@code true} if {@code object} is found in {@code array}; {@code false} otherwise
     */
    public <T> boolean contains(T[] array, T object) {
        return toList(array).contains(object);
    }

    /**
     * Determines if an {@link Iterable} is null or contains no elements.
     *
     * @param  <T>      the element type
     * @param  iterable the iterable to check
     * @return          {@code true} if {@code iterable} is null or has no elements; {@code false} otherwise
     */
    public <T> boolean isIterableEmpty(Iterable<T> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }
}
