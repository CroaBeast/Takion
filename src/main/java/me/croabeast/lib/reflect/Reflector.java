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
 * Reflector is a class designed to simplify reflection operations.
 *
 * <p> It allows for easy access and manipulation of methods, fields, and constructors
 * of a specified class.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public final class Reflector {

    private static final String API = ServerInfoUtils.BUKKIT_API_VERSION;

    /**
     * The base package name for NMS classes.
     */
    public static final String NMS_PACKAGE = "net.minecraft.server." + API + (StringUtils.isNotBlank(API) ? "." : "");

    /**
     * The base package name for CraftBukkit classes.
     */
    public static final String CRAFT_BUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    private final Class<?> clazz;
    @Getter
    private Supplier<Object> initial = null;

    private Object fromInitial(Object o) {
        return o != null ? o : (initial != null ? initial.get() : null);
    }

    /**
     * Sets the initial object supplier for this Reflector.
     *
     * @param supplier the object supplier
     * @return this Reflector instance
     */
    public Reflector setInitial(Supplier<Object> supplier) {
        this.initial = Objects.requireNonNull(supplier);
        return this;
    }

    /**
     * Gets the type of the reflected class.
     *
     * @return the class type
     */
    public Class<?> getType() {
        return clazz;
    }

    /**
     * Gets all methods of the reflected class.
     *
     * @return a list of methods
     */
    public List<Method> getMethods() {
        return ArrayUtils.toList(clazz.getDeclaredMethods());
    }

    /**
     * Gets all fields of the reflected class.
     *
     * @return a list of fields
     */
    public List<Field> getFields() {
        return ArrayUtils.toList(clazz.getDeclaredFields());
    }

    /**
     * Gets all constructors of the reflected class.
     *
     * @return a list of constructors
     */
    public List<Constructor<?>> getConstructors() {
        return ArrayUtils.toList(clazz.getDeclaredConstructors());
    }

    /**
     * Gets a specific method by name and parameter types.
     *
     * @param name the method name
     * @param parameters the method parameter types
     * @return the method
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
     * Gets a specific field by name.
     *
     * @param name the field name
     * @return the field
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
     * Gets a specific field by type.
     *
     * @param clazz the field type
     * @return the field
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
     * Gets a specific constructor by parameter types.
     *
     * @param parameters the constructor parameter types
     * @return the constructor
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
     * Calls a method on the initial object or a specified object.
     *
     * @param initial    the initial object (optional)
     * @param methodName the method name
     * @param objects    the method arguments
     * @param <T>        the return type
     * @return the result of the method call
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
     * Calls a method on the initial object.
     *
     * @param methodName the method name
     * @param objects    the method arguments
     * @param <T>        the return type
     * @return the result of the method call
     */
    public <T> T call(String methodName, Object... objects) {
        return call(null, methodName, objects);
    }

    /**
     * Gets a field value from the initial object or a specified object.
     *
     * @param initial   the initial object (optional)
     * @param fieldName the field name
     * @param <T>       the field type
     * @return the field value
     */
    @SneakyThrows
    public <T> T get(Object initial, String fieldName) {
        return (T) getField(fieldName).get(fromInitial(initial));
    }

    /**
     * Gets a field value from the initial object.
     *
     * @param fieldName the field name
     * @param <T>       the field type
     * @return the field value
     */
    public <T> T get(String fieldName) {
        return get(null, fieldName);
    }

    /**
     * Gets a field value of a specific type from the initial object or a specified object.
     *
     * @param initial the initial object (optional)
     * @param clazz   the field type
     * @param <T>     the field type
     * @return the field value
     */
    @SneakyThrows
    public <T> T get(Object initial, Class<?> clazz) {
        return (T) getField(clazz).get(fromInitial(initial));
    }

    /**
     * Gets a field value of a specific type from the initial object.
     *
     * @param clazz the field type
     * @param <T>   the field type
     * @return the field value
     */
    public <T> T get(Class<?> clazz) {
        return get(null, clazz);
    }

    /**
     * Sets a field value on the initial object or a specified object.
     *
     * @param initial   the initial object (optional)
     * @param fieldName the field name
     * @param value     the value to set
     */
    @SneakyThrows
    public void set(Object initial, String fieldName, Object value) {
        getField(fieldName).set(fromInitial(initial), value);
    }

    /**
     * Sets a field value on the initial object.
     *
     * @param fieldName the field name
     * @param value     the value to set
     */
    public void set(String fieldName, Object value) {
        set(null, fieldName, value);
    }

    /**
     * Creates an instance of the reflected class using the specified constructor arguments.
     *
     * @param objects the constructor arguments
     * @param <T>     the instance type
     * @return the new instance
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

    @SneakyThrows
    public Reflector asReflector(Object... objects) {
        return Reflector.from(() -> create(objects));
    }

    /**
     * Creates a Reflector for the specified class.
     *
     * @param clazz the class to reflect
     * @return a new Reflector instance
     */
    public static Reflector of(Class<?> clazz) {
        return new Reflector(Objects.requireNonNull(clazz));
    }

    /**
     * Creates a Reflector for the specified class name.
     *
     * @param path the class name
     * @return a new Reflector instance
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
     * Creates a Reflector for the specified NMS class name.
     *
     * @param className the NMS class name
     * @return a new Reflector instance
     */
    public static Reflector ofNms(String className) {
        return of(NMS_PACKAGE + className);
    }

    /**
     * Creates a Reflector for the specified CraftBukkit class name.
     *
     * @param className the CraftBukkit class name
     * @return a new Reflector instance
     */
    public static Reflector ofCraftBukkit(String className) {
        return of(CRAFT_BUKKIT_PACKAGE + className);
    }

    /**
     * Creates a Reflector from a supplier providing an initial object.
     *
     * @param supplier the object supplier
     * @return a new Reflector instance
     */
    public static Reflector from(Supplier<Object> supplier) {
        return new Reflector(supplier.get().getClass()).setInitial(supplier);
    }
}
