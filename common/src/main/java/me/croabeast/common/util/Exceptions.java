package me.croabeast.common.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A collection of utility methods for runtime checks and validations,
 * such as verifying plugin availability and enforcing preconditions.
 *
 * <p>This class provides methods to:
 * <ul>
 *   <li>Check if a plugin is installed or enabled on the server.</li>
 *   <li>Require certain conditions on objects, throwing exceptions if violated.</li>
 *   <li>Ensure that code executing on behalf of one plugin cannot be called by another.</li>
 * </ul>
 * </p>
 *
 * @see Bukkit#getPluginManager()
 * @see Objects#requireNonNull(Object)
 */
@UtilityClass
public class Exceptions {

    /**
     * Checks if the given plugin is enabled (loaded and running).
     *
     * @param name the name of the plugin (case-sensitive)
     * @return     {@code true} if the plugin is installed and enabled; {@code false} otherwise
     */
    public boolean isPluginEnabled(@NotNull String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    /**
     * Checks if at least one of the specified plugins is enabled.
     *
     * @param names the plugin names to check
     * @return      {@code true} if any plugin in {@code names} is enabled; {@code false} if none are enabled or if {@code names} is empty
     * @deprecated use {@link #anyPluginEnabled(Collection)} instead for clarity
     */
    @Deprecated
    public boolean arePluginsEnabled(boolean inclusive, @NotNull Collection<String> names) {
        if (names.isEmpty()) return false;
        for (String name : names) {
            boolean enabled = isPluginEnabled(name);
            if (!inclusive && enabled) return true;
            if ( inclusive && !enabled) return false;
        }
        return inclusive;
    }

    /**
     * Returns {@code true} if any of the provided plugin names correspond to an enabled plugin.
     *
     * @param names the plugin names to check
     * @return      {@code true} if at least one plugin is enabled; {@code false} otherwise
     */
    public boolean anyPluginEnabled(@NotNull Collection<String> names) {
        return names.stream().anyMatch(Exceptions::isPluginEnabled);
    }

    /**
     * Returns {@code true} only if all of the provided plugin names correspond to enabled plugins.
     *
     * @param names the plugin names to check
     * @return      {@code true} if every plugin is enabled; {@code false} otherwise
     */
    public boolean allPluginsEnabled(@NotNull Collection<String> names) {
        return !names.isEmpty() && names.stream().allMatch(Exceptions::isPluginEnabled);
    }

    /**
     * Ensures that the given object satisfies the provided predicate.
     * <p>
     * If the predicate test fails, the supplied exception is thrown.
     * </p>
     *
     * @param  <T>               the type of the input object
     * @param  object            the object to validate (must not be null)
     * @param  predicate         the condition to test
     * @param  supplier supplies the exception to throw if the test fails
     *
     * @return                   the validated object
     * @throws X                 the exception returned by {@code supplier}
     */
    public <T, X extends Throwable> T validate(T object, Predicate<T> predicate, Supplier<X> supplier) throws X {
        Objects.requireNonNull(object);
        if (!predicate.test(object)) throw supplier.get();
        return object;
    }

    /**
     * Ensures that the given object satisfies the provided predicate.
     * <p>
     * If the predicate test fails, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param  <T>               the type of the input object
     * @param  object            the object to validate (must not be null)
     * @param  predicate         the condition to test
     * @param  errorMessage      the error message to be shown
     *
     * @return                   the validated object
     * @throws IllegalStateException if the {@code predicate} test fails
     */
    @NotNull
    public <T> T validate(T object, Predicate<T> predicate, String errorMessage) throws IllegalStateException {
        return validate(object, predicate, () -> new IllegalStateException(errorMessage));
    }

    /**
     * Ensures that the given object satisfies the provided predicate.
     * <p>
     * If the predicate test fails, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param  <T>               the type of the input object
     * @param  object            the object to validate (must not be null)
     * @param  predicate         the condition to test
     *
     * @return                   the validated object
     * @throws IllegalStateException if the {@code predicate} test fails
     */
    @NotNull
    public <T> T validate(T object, Predicate<T> predicate) throws IllegalStateException {
        return validate(object, predicate, IllegalStateException::new);
    }

    /**
     * Ensures that the given object satisfies the provided predicate.
     * <p>
     * If the predicate test fails, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param  <T>               the type of the input object
     * @param  object            the object to validate (must not be null)
     * @param  b                 the condition to test
     *
     * @return                   the validated object
     * @throws IllegalStateException if the {@code predicate} test fails
     */
    @NotNull
    public <T> T validate(T object, boolean b) {
        return validate(object, t -> b);
    }

    /**
     * Ensures that the caller of the protected API belongs to the given plugin.
     * <p>
     * If the calling class was not provided by {@code plugin}, throws the supplied exception.
     * </p>
     *
     * @param  plugin    the plugin that must own the caller class (might be null)
     * @param  clazz the class invoking the protected API
     *
     * @throws IllegalStateException    if {@code clazz} was not provided by {@code plugin}
     */
    public void requirePluginAccess(@Nullable Plugin plugin, @NotNull Class<?> clazz) {
        Plugin owner = null;
        try {
            owner = JavaPlugin.getProvidingPlugin(clazz);
        } catch (Throwable ignore) {}

        if (!Objects.equals(owner, plugin))
            throw new IllegalStateException();
    }

    /**
     * Retrieves the class of a caller up the current thread's stack trace.
     * <p>
     * <strong>Edge cases:</strong> May return unexpected results under aggressive JVM inlining.
     * </p>
     *
     * @param  depth the zero-based index into the stack trace (0 is this method)
     * @return       the {@link Class} object at the requested stack frame
     * @throws ClassNotFoundException if the class name cannot be resolved
     * @deprecated this approach is fragile; consider passing explicit context instead
     */
    @Deprecated
    public Class<?> getCallerClass(int depth) throws ClassNotFoundException {
        StackTraceElement[] array = Thread.currentThread().getStackTrace();
        String name = array[validate(depth, d -> d >= 0)].getClassName();
        return Class.forName(name);
    }
}