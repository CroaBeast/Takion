package me.croabeast.lib.reflect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.croabeast.lib.util.ArrayUtils;
import me.croabeast.lib.util.Exceptions;
import me.croabeast.lib.util.ServerInfoUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A powerful utility for performing reflection operations in a fluent and concise manner.
 * <p>
 * The {@code Reflector} class encapsulates a target class and provides methods to retrieve
 * its constructors, fields, methods, and inner classes. It also offers helper functions to
 * invoke methods, access and modify fields, and create new instances dynamically.
 * </p>
 * <p>
 * This tool is particularly useful in plugin development where access to internal server classes
 * (such as NMS or CraftBukkit classes) is needed. It abstracts away the boilerplate associated with
 * reflection and helps in writing more readable and maintainable code.
 * </p>
 * <p>
 * <strong>Example usage:</strong>
 * <pre><code>
 * // Create a Reflector for a target class by its fully-qualified name:
 * Reflector reflector = Reflector.of("net.minecraft.server.v1_16_R3.EntityPlayer");
 *
 * // Retrieve a declared method and invoke it:
 * Method method = reflector.getMethod("someInternalMethod", String.class);
 * Object result = method.invoke(someEntityPlayerInstance, "argument");
 *
 * // Alternatively, invoke a method using the fluent call:
 * Object resultFluent = reflector.call("someInternalMethod", "argument");
 * </code></pre>
 * </p>
 *
 * <p>
 * Additionally, the {@code Reflector} supports creating new instances, accessing private fields,
 * and converting return values into new Reflector objects for further chained operations.
 * </p>
 *
 * @see java.lang.reflect
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public final class Reflector {

    /**
     * The current Bukkit API version, used for dynamic resolution of NMS classes.
     */
    private static final String API = ServerInfoUtils.BUKKIT_API_VERSION;

    /**
     * The package path for NMS (net.minecraft.server) classes, dynamically constructed based on the API version.
     */
    public static final String NMS_PACKAGE = "net.minecraft.server." + API + (StringUtils.isNotBlank(API) ? "." : "");

    /**
     * The package path for CraftBukkit classes, derived from the server's implementation.
     */
    public static final String CRAFT_BUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /**
     * The target class that this Reflector instance wraps.
     */
    private final Class<?> clazz;

    /**
     * An optional supplier used to provide an initial object instance for non-static reflection operations.
     */
    @Getter
    private Supplier<Object> initial = null;

    /**
     * Returns an object from the initial supplier if available; otherwise returns the provided object.
     *
     * @param o the object to fallback to if the initial supplier returns {@code null}
     * @return the object from the initial supplier or the fallback object
     */
    private Object fromInitial(Object o) {
        return o != null ? o : (initial != null ? initial.get() : null);
    }

    /**
     * Sets the supplier for the initial object instance. This can be used to delay the instantiation
     * or to provide dynamic values for non-static method invocations.
     *
     * @param supplier the supplier to set (must not be null)
     * @return this Reflector instance for chaining
     */
    public Reflector setInitial(Supplier<Object> supplier) {
        this.initial = Objects.requireNonNull(supplier);
        return this;
    }

    /**
     * Returns the target class wrapped by this Reflector.
     *
     * @return the target {@code Class<?>}
     */
    public Class<?> getType() {
        return clazz;
    }

    /**
     * Retrieves a list of all declared methods of the target class.
     *
     * @return a {@code List} of {@link Method} objects representing the declared methods
     */
    public List<Method> getMethods() {
        return ArrayUtils.toList(clazz.getDeclaredMethods());
    }

    /**
     * Retrieves a list of all declared fields of the target class.
     *
     * @return a {@code List} of {@link Field} objects representing the declared fields
     */
    public List<Field> getFields() {
        return ArrayUtils.toList(clazz.getDeclaredFields());
    }

    /**
     * Retrieves a list of all declared constructors of the target class.
     *
     * @return a {@code List} of {@link Constructor} objects representing the declared constructors
     */
    public List<Constructor<?>> getConstructors() {
        return ArrayUtils.toList(clazz.getDeclaredConstructors());
    }

    /**
     * Retrieves a list of all declared inner classes of the target class.
     *
     * @return a {@code List} of {@link Class} objects representing the declared inner classes
     */
    public List<Class<?>> getClasses() {
        return ArrayUtils.toList(clazz.getDeclaredClasses());
    }

    /**
     * Retrieves a declared method by name and parameter types.
     * <p>
     * If the method is not accessible, its accessibility is set to {@code true}.
     * </p>
     *
     * @param name       the name of the method
     * @param parameters the parameter types of the method
     * @return the {@link Method} matching the given signature
     * @throws NullPointerException if the method is not found
     */
    public Method getMethod(String name, Class<?>... parameters) {
        Method method;
        try {
            method = clazz.getDeclaredMethod(name, parameters);
        } catch (Exception e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
        if (!method.isAccessible()) method.setAccessible(true);
        return method;
    }

    /**
     * Retrieves a declared field by name.
     * <p>
     * If the field is not accessible, its accessibility is set to {@code true}.
     * </p>
     *
     * @param name the name of the field
     * @return the {@link Field} with the specified name
     * @throws NullPointerException if the field is not found
     */
    public Field getField(String name) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (Exception e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
        if (!field.isAccessible()) field.setAccessible(true);
        return field;
    }

    /**
     * Retrieves a declared field from the specified class that is not of the same type as the class itself.
     * <p>
     * This method is useful when trying to find a field that holds an instance of a different type.
     * </p>
     *
     * @param clazz the class in which to search for the field
     * @return the first {@link Field} that does not have the same type as the provided class
     * @throws NullPointerException if no such field is found
     */
    public Field getField(Class<?> clazz) {
        Field field = null;
        for (Field f : clazz.getDeclaredFields())
            if (!Objects.equals(f.getType(), clazz)) {
                field = f;
                break;
            }
        Objects.requireNonNull(field);
        if (!field.isAccessible())
            field.setAccessible(true);
        return field;
    }

    /**
     * Retrieves a declared constructor with the specified parameter types.
     * <p>
     * If the constructor is not accessible, its accessibility is set to {@code true}.
     * </p>
     *
     * @param parameters the parameter types of the constructor
     * @return the {@link Constructor} matching the given signature
     * @throws NullPointerException if the constructor is not found
     */
    public Constructor<?> getConstructor(Class<?>... parameters) {
        Constructor<?> c;
        try {
            c = clazz.getDeclaredConstructor(parameters);
        } catch (Exception e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
        if (!c.isAccessible()) c.setAccessible(true);
        return c;
    }

    /**
     * Invokes a method on the target object (or an object provided by the initial supplier) with the given parameters.
     * <p>
     * The method is looked up by name and parameter types inferred from the provided arguments.
     * </p>
     *
     * @param <T>        the expected return type
     * @param initial    an optional initial object to invoke the method on; if {@code null}, the supplier's value is used
     * @param methodName the name of the method to invoke
     * @param objects    the arguments to pass to the method
     * @return the result of the method invocation, cast to type {@code T}
     */
    @SneakyThrows
    public <T> T call(Object initial, String methodName, Object... objects) {
        if (ArrayUtils.isArrayEmpty(objects)) objects = new Object[0];
        Class<?>[] classes = new Class[objects.length];
        if (objects.length > 0)
            for (int i = 0; i < objects.length; i++)
                classes[i] = objects[i].getClass();
        return (T) getMethod(methodName, classes).invoke(fromInitial(initial), objects);
    }

    /**
     * Invokes a method on the target object using the specified method name and parameters.
     * <p>
     * This is a convenience overload that assumes a {@code null} initial object.
     * </p>
     *
     * @param <T>        the expected return type
     * @param methodName the name of the method to invoke
     * @param objects    the arguments to pass to the method
     * @return the result of the method invocation, cast to type {@code T}
     */
    public <T> T call(String methodName, Object... objects) {
        return call(null, methodName, objects);
    }

    /**
     * Invokes a method on the target object and wraps the result in a new {@code Reflector} for further reflection.
     *
     * @param initial    an optional initial object to invoke the method on
     * @param methodName the name of the method to invoke
     * @param objects    the arguments to pass to the method
     * @return a new {@code Reflector} wrapping the result of the method call
     */
    public Reflector callAsReflector(Object initial, String methodName, Object... objects) {
        return Reflector.from(() -> call(initial, methodName, objects));
    }

    /**
     * Invokes a method on the target object and wraps the result in a new {@code Reflector} for further reflection.
     *
     * @param methodName the name of the method to invoke
     * @param objects    the arguments to pass to the method
     * @return a new {@code Reflector} wrapping the result of the method call
     */
    public Reflector callAsReflector(String methodName, Object... objects) {
        return Reflector.from(() -> call(methodName, objects));
    }

    /**
     * Retrieves the value of a field from the target object (or an object provided by the initial supplier).
     *
     * @param <T>       the expected type of the field value
     * @param initial   an optional initial object from which to retrieve the field value; if {@code null}, the supplier's value is used
     * @param fieldName the name of the field
     * @return the value of the field, cast to type {@code T}
     */
    @SneakyThrows
    public <T> T get(Object initial, String fieldName) {
        return (T) getField(fieldName).get(fromInitial(initial));
    }

    /**
     * Retrieves the value of a field with the given name from the target object.
     *
     * @param <T>       the expected type of the field value
     * @param fieldName the name of the field
     * @return the field value, cast to type {@code T}
     */
    public <T> T get(String fieldName) {
        return get(null, fieldName);
    }

    /**
     * Retrieves the value of a field (by matching type) from the target object.
     *
     * @param <T>     the expected type of the field value
     * @param clazz   the type of the field to retrieve
     * @param initial an optional initial object from which to retrieve the field value; if {@code null}, the supplier's value is used
     * @return the field value, cast to type {@code T}
     */
    @SneakyThrows
    public <T> T get(Object initial, Class<?> clazz) {
        return (T) getField(clazz).get(fromInitial(initial));
    }

    /**
     * Retrieves the value of a field (by matching type) from the target object.
     *
     * @param <T>   the expected type of the field value
     * @param clazz the type of the field to retrieve
     * @return the field value, cast to type {@code T}
     */
    public <T> T get(Class<?> clazz) {
        return get(null, clazz);
    }

    /**
     * Retrieves a field value and wraps it in a new {@code Reflector} for further reflective operations.
     *
     * @param initial   an optional initial object from which to retrieve the field value
     * @param fieldName the name of the field
     * @return a new {@code Reflector} wrapping the retrieved field value
     */
    @SneakyThrows
    public Reflector getAsReflector(Object initial, String fieldName) {
        return Reflector.from(() -> get(initial, fieldName));
    }

    /**
     * Retrieves a field value by name and wraps it in a new {@code Reflector} for further reflective operations.
     *
     * @param fieldName the name of the field
     * @return a new {@code Reflector} wrapping the retrieved field value
     */
    @SneakyThrows
    public Reflector getAsReflector(String fieldName) {
        return Reflector.from(() -> get(fieldName));
    }

    /**
     * Retrieves a field value (by matching type) and wraps it in a new {@code Reflector} for further reflective operations.
     *
     * @param initial an optional initial object from which to retrieve the field value
     * @param clazz   the type of the field to retrieve
     * @return a new {@code Reflector} wrapping the retrieved field value
     */
    @SneakyThrows
    public Reflector getAsReflector(Object initial, Class<?> clazz) {
        return Reflector.from(() -> get(initial, clazz));
    }

    /**
     * Retrieves a field value (by matching type) and wraps it in a new {@code Reflector} for further reflective operations.
     *
     * @param clazz the type of the field to retrieve
     * @return a new {@code Reflector} wrapping the retrieved field value
     */
    @SneakyThrows
    public Reflector getAsReflector(Class<?> clazz) {
        return Reflector.from(() -> get(clazz));
    }

    /**
     * Sets the value of a field with the specified name on the target object (or an object provided by the initial supplier).
     *
     * @param initial   an optional initial object on which to set the field value; if {@code null}, the supplier's value is used
     * @param fieldName the name of the field
     * @param value     the value to set in the field
     */
    @SneakyThrows
    public void set(Object initial, String fieldName, Object value) {
        getField(fieldName).set(fromInitial(initial), value);
    }

    /**
     * Sets the value of a field with the specified name on the target object.
     *
     * @param fieldName the name of the field
     * @param value     the value to set in the field
     */
    public void set(String fieldName, Object value) {
        set(null, fieldName, value);
    }

    /**
     * Creates a new instance of the target class using the constructor that matches the provided arguments.
     *
     * @param objects the arguments to pass to the constructor
     * @param <T>     the type of the created instance
     * @return a new instance of type {@code T}
     */
    @SneakyThrows
    public <T> T create(Object... objects) {
        if (ArrayUtils.isArrayEmpty(objects)) objects = new Object[0];
        Class<?>[] classes = new Class[objects.length];
        if (objects.length > 0)
            for (int i = 0; i < objects.length; i++)
                classes[i] = objects[i].getClass();
        return (T) getConstructor(classes).newInstance(objects);
    }

    /**
     * Creates a new instance of the target class using the provided constructor arguments,
     * and wraps the resulting object in a {@code Reflector} for further reflection.
     *
     * @param objects the arguments to pass to the constructor
     * @return a new {@code Reflector} wrapping the created instance
     */
    @SneakyThrows
    public Reflector createAsReflector(Object... objects) {
        return Reflector.from(() -> create(objects));
    }

    /**
     * Creates a new {@code Reflector} for the specified target class.
     *
     * @param clazz the target class
     * @return a new {@code Reflector} wrapping the target class
     */
    public static Reflector of(Class<?> clazz) {
        return new Reflector(Objects.requireNonNull(clazz));
    }

    /**
     * Creates a new {@code Reflector} for the class with the specified fully-qualified name.
     *
     * @param path the fully-qualified class name
     * @return a new {@code Reflector} wrapping the target class
     * @throws IllegalStateException if the class cannot be found
     */
    public static Reflector of(String path) {
        Exceptions.validate(StringUtils::isNotBlank, path);
        Class<?> clazz;
        try {
            clazz = Class.forName(path);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return of(clazz);
    }

    /**
     * Creates a new {@code Reflector} for an NMS (net.minecraft.server) class with the given name.
     *
     * @param className the name of the NMS class
     * @return a new {@code Reflector} wrapping the NMS class
     */
    public static Reflector ofNms(String className) {
        return of(NMS_PACKAGE + className);
    }

    /**
     * Creates a new {@code Reflector} for a CraftBukkit class with the given name.
     *
     * @param className the name of the CraftBukkit class
     * @return a new {@code Reflector} wrapping the CraftBukkit class
     */
    public static Reflector ofCraftBukkit(String className) {
        return of(CRAFT_BUKKIT_PACKAGE + className);
    }

    /**
     * Creates a new {@code Reflector} from a supplier that provides an object instance.
     * <p>
     * The supplier is stored as the initial supplier for this reflector, allowing future operations
     * to work with the supplied instance.
     * </p>
     *
     * @param supplier a supplier that provides an object instance
     * @return a new {@code Reflector} wrapping the class of the supplied object
     */
    public static Reflector from(Supplier<Object> supplier) {
        return new Reflector(supplier.get().getClass()).setInitial(supplier);
    }
}
