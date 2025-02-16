package me.croabeast.lib.file;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

final class FileLoader {

    private final Method resourceMethod;
    private final Method folderMethod;

    final Object loader;

    <T> FileLoader(T loader) throws IOException {
        Class<?> clazz = loader.getClass();

        try {
            resourceMethod = clazz.getMethod("getResource", String.class);
            folderMethod = clazz.getMethod("getDataFolder");

            this.loader = loader;
        } catch (Exception e) {
            throw new IOException("Loader object isn't valid");
        }
    }

    @SneakyThrows
    InputStream getResource(String name) {
        return (InputStream) resourceMethod.invoke(loader, name);
    }

    @SneakyThrows
    File getDataFolder() {
        return (File) folderMethod.invoke(loader);
    }
}

