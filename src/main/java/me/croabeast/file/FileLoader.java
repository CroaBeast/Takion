package me.croabeast.file;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * A utility class for loading resources and obtaining the data folder from a provided loader object.
 * <p>
 * {@code FileLoader} uses reflection to invoke the {@code getResource(String)} and {@code getDataFolder()}
 * methods on the given loader object. This allows the library to work with different loader types (for example,
 * a plugin instance) without hardcoding the loader's class.
 * </p>
 * <p>
 * If the loader object does not have the expected methods, an {@link IOException} is thrown during construction.
 * </p>
 *
 * @see InputStream
 * @see File
 */
final class FileLoader {

    /**
     * The Method used to obtain a resource input stream from the loader.
     */
    private final Method resourceMethod;

    /**
     * The Method used to obtain the data folder from the loader.
     */
    private final Method folderMethod;

    /**
     * The loader object from which resources and data folder are retrieved.
     */
    final Object loader;

    /**
     * Constructs a new {@code FileLoader} using the specified loader object.
     * <p>
     * The constructor attempts to locate and store the {@code getResource(String)} and
     * {@code getDataFolder()} methods from the loader's class. If these methods are not found,
     * an {@link IOException} is thrown indicating that the loader object is invalid.
     * </p>
     *
     * @param loader the loader object (e.g., a plugin instance) to use for resource loading.
     * @param <T>    the type of the loader object.
     * @throws IOException if the loader object does not have the required methods.
     */
    <T> FileLoader(T loader) throws IOException {
        Class<?> clazz = loader.getClass();
        try {
            resourceMethod = clazz.getMethod("getResource", String.class);
            folderMethod = clazz.getMethod("getDataFolder");
            this.loader = loader;
        } catch (Exception e) {
            throw new IOException("Loader object isn't valid", e);
        }
    }

    /**
     * Retrieves the resource as an {@link InputStream} from the loader using the specified resource name.
     * <p>
     * This method uses reflection to invoke the {@code getResource(String)} method on the loader object.
     * </p>
     *
     * @param name the name of the resource to retrieve.
     * @return an {@link InputStream} for the specified resource.
     * @throws Exception if an error occurs during method invocation.
     */
    @SneakyThrows
    InputStream getResource(String name) {
        return (InputStream) resourceMethod.invoke(loader, name);
    }

    /**
     * Retrieves the data folder as a {@link File} from the loader.
     * <p>
     * This method uses reflection to invoke the {@code getDataFolder()} method on the loader object.
     * </p>
     *
     * @return a {@link File} representing the data folder.
     * @throws Exception if an error occurs during method invocation.
     */
    @SneakyThrows
    File getDataFolder() {
        return (File) folderMethod.invoke(loader);
    }
}
