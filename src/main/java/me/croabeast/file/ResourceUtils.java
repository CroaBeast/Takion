package me.croabeast.file;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

/**
 * The {@code ResourceUtils} class provides utility methods for handling file resources,
 * such as saving resources to the file system and constructing file paths.
 * <p>
 * It contains methods to save an {@link InputStream} resource to a specified location within
 * a data folder and to resolve file paths relative to a parent directory.
 * </p>
 *
 * @see InputStream
 * @see File
 * @see OutputStream
 */
@UtilityClass
public class ResourceUtils {

    /**
     * Saves the specified resource to the given file path within the data folder.
     * <p>
     * If the target directory does not exist, it will be created. If a file already exists at the
     * target location and {@code replace} is {@code false}, an {@link UnsupportedOperationException} is thrown.
     * </p>
     *
     * @param resource  the input stream of the resource to save; must not be {@code null}.
     * @param dataFolder the data folder where the resource will be saved; must not be {@code null}.
     * @param path      the file path within the data folder to save the resource to; must not be {@code null}.
     * @param replace   whether to replace the existing file if it already exists.
     *
     * @throws NullPointerException if the {@code path} or {@code resource} is {@code null}.
     * @throws UnsupportedOperationException if the file already exists and {@code replace} is {@code false}, or if an IO error occurs during saving.
     */
    public void saveResource(@Nullable InputStream resource, File dataFolder, String path, boolean replace) {
        if (path == null || resource == null)
            throw new NullPointerException("Path or resource is null");

        path = path.replace('\\', '/');
        int lastIndex = path.lastIndexOf('/');

        File out = new File(dataFolder, path);
        File dir = new File(dataFolder, path.substring(0, Math.max(lastIndex, 0)));

        if (!dir.exists()) dir.mkdirs();
        if (out.exists() && !replace)
            throw new UnsupportedOperationException("File already exists");

        try {
            OutputStream o = Files.newOutputStream(out.toPath());
            byte[] buf = new byte[1024];
            int len;
            while ((len = resource.read(buf)) > 0)
                o.write(buf, 0, len);
            o.close();
            resource.close();
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Saves the specified resource to the given file path within the data folder.
     * <p>
     * This method behaves like {@link #saveResource(InputStream, File, String, boolean)} with
     * {@code replace} set to {@code false}. Existing files will not be replaced.
     * </p>
     *
     * @param resource  the input stream of the resource to save; must not be {@code null}.
     * @param dataFolder the data folder where the resource will be saved; must not be {@code null}.
     * @param path      the file path within the data folder to save the resource to; must not be {@code null}.
     *
     * @throws NullPointerException if the {@code path} or {@code resource} is {@code null}.
     * @throws UnsupportedOperationException if the file already exists or if an IO error occurs during saving.
     */
    public void saveResource(@Nullable InputStream resource, File dataFolder, String path) {
        saveResource(resource, dataFolder, path, false);
    }

    /**
     * Creates a {@link File} object by resolving the specified child file paths against the given parent directory.
     * <p>
     * The method iteratively appends each element in {@code childPaths} to the {@code parent} directory,
     * producing a final {@link File} that represents the nested file location.
     * </p>
     *
     * @param parent     the parent directory to resolve the child file paths against; must not be {@code null}.
     * @param childPaths the relative paths of the child files.
     * @return the {@link File} object representing the specified child file paths within the parent directory.
     * @throws NullPointerException if the {@code parent} directory is {@code null}.
     */
    public File fileFrom(@Nullable File parent, String... childPaths) {
        Objects.requireNonNull(parent);
        if (childPaths == null || childPaths.length < 1)
            return parent;
        for (String child : childPaths)
            parent = new File(parent, child);
        return parent;
    }
}
