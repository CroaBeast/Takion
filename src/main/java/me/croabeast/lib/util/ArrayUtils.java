package me.croabeast.lib.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Utility class for common array operations.
 *
 * <p> This class provides a variety of methods for manipulating and converting arrays,
 * as well as performing operations on collections.
 */
@UtilityClass
public class ArrayUtils {

    /**
     * Combines multiple arrays into a single array.
     *
     * @param array the initial array.
     * @param extraArrays additional arrays to combine with the initial array.
     * @param <T> the type of elements in the arrays.
     *
     * @author Kihsomray
     * @since 1.3
     *
     * @return a new array containing all elements from the input arrays.
     */
    @SafeVarargs
    public <T> T[] combineArrays(@NotNull T[] array, T[]... extraArrays) {
        if (isArrayEmpty(extraArrays)) return array;

        List<T> resultList = new ArrayList<>();
        Collections.addAll(resultList, array);

        for (T[] a : extraArrays)
            if (a != null) Collections.addAll(resultList, a);

        Class<?> clazz = array.getClass().getComponentType();
        T[] resultArray = (T[]) Array.newInstance(clazz, 0);

        return resultList.toArray(resultArray);
    }

    /**
     * Checks if an array is empty.
     *
     * @param array the array to check.
     * @param <T> the type of elements in the array.
     *
     * @return true if the array is null or has no elements, false otherwise.
     */
    @SafeVarargs
    public <T> boolean isArrayEmpty(T... array) {
        return array == null || array.length < 1;
    }

    /**
     * Validates that an array is not empty.
     *
     * @param array the array to check.
     * @param <T> the type of elements in the array.
     *
     * @return the input array if it is not empty.
     * @throws IllegalArgumentException if the array is empty.
     */
    @SafeVarargs
    public <T> T[] checkArray(T... array) {
        return Exceptions.validate(ArrayUtils::isArrayEmpty, array, "Array should be declared");
    }

    /**
     * Converts an array to a collection, applying a function to each element.
     *
     * @param collection the collection to populate.
     * @param function the function to apply to each element.
     * @param array the array to convert.
     *
     * @param <T> the type of elements in the array.
     * @param <I> the type of the collection.
     *
     * @return the populated collection.
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, UnaryOperator<T> function, T... array) {
        Objects.requireNonNull(collection);

        if (isArrayEmpty(array)) return collection;

        for (T o : array) {
            o = function == null ? o : function.apply(o);
            collection.add(o);
        }

        return collection;
    }

    /**
     * Converts an array to a collection without applying any function.
     *
     * @param collection the collection to populate.
     * @param array the array to convert.
     *
     * @param <T> the type of elements in the array.
     * @param <I> the type of the collection.
     *
     * @return the populated collection.
     */
    @SafeVarargs
    public <T, I extends Collection<T>> I toCollection(I collection, T... array) {
        return toCollection(collection, null, array);
    }

    /**
     * Converts an array to a list, applying a function to each element.
     *
     * @param operator the function to apply to each element.
     * @param array the array to convert.
     * @param <T> the type of elements in the array.
     *
     * @return a list containing the transformed elements.
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> toList(UnaryOperator<T> operator, T... array) {
        return toCollection(new ArrayList<>(), operator, array);
    }

    /**
     * Converts an array to a list without applying any function.
     *
     * @param array the array to convert.
     * @param <T> the type of elements in the array.
     *
     * @return a list containing the elements of the array.
     */
    @SafeVarargs
    @NotNull
    public <T> List<T> toList(T... array) {
        return toCollection(new ArrayList<>(), array);
    }

    /**
     * Converts an array to a list without applying any function.
     *
     * @param array the array to convert.
     * @param <T> the type of elements in the array.
     *
     * @return a list containing the elements of the array.
     */
    @SafeVarargs
    @NotNull
    public <T> Set<T> toSet(T... array) {
        return toCollection(new HashSet<>(), array);
    }

    /**
     * Applies a series of functions to each element in an array.
     *
     * @param array the array to transform.
     * @param operators the functions to apply to each element.
     * @param <T> the type of elements in the array.
     *
     * @return the transformed array.
     */
    @SafeVarargs
    public <T> T[] applyToArray(T[] array, UnaryOperator<T>... operators) {
        Objects.requireNonNull(array);

        for (int i = 0; i < array.length; i++) {
            T temp = array[i];

            for (UnaryOperator<T> o : toList(operators))
                temp = o.apply(temp);

            array[i] = temp;
        }

        return array;
    }

    /**
     * Checks if an array contains a specific element.
     *
     * @param array the array to check.
     * @param object the element to search for.
     * @param <T> the type of elements in the array.
     *
     * @return true if the array contains the element, false otherwise.
     */
    public <T> boolean contains(T[] array, T object) {
        return toList(array).contains(object);
    }

    /**
     * Checks if an iterable is empty.
     *
     * @param iterable the iterable to check.
     * @param <T> the type of elements in the iterable.
     *
     * @return true if the iterable is null or has no elements, false otherwise.
     */
    public <T> boolean isIterableEmpty(Iterable<T> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }
}
