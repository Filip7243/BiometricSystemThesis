package com.bio.bio_backend.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static com.neurotec.samples.util.Utils.PATH_SEPARATOR;
import static com.neurotec.samples.util.Utils.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class LibraryManager {
    private final static String NATIVE_FOLDER_NAME = "native";

    public static void initLibraryPath() {
        String libraryPath = getLibraryPath();
        String jnaLibraryPath = getProperty("jna.library.path");
        if (isNullOrEmpty(libraryPath)) {
            return;
        }

        if (isNullOrEmpty(jnaLibraryPath)) {
            System.setProperty("jna.library.path", libraryPath);
        } else {
            System.setProperty("jna.library.path", format("%s%s%s", jnaLibraryPath, PATH_SEPARATOR, libraryPath));
        }
        System.setProperty("java.library.path", format("%s%s%s", getProperty("java.library.path"), PATH_SEPARATOR, libraryPath));
    }

    private static String getLibraryPath() {
        try {
            URL resourceUrl = LibraryManager.class.getClassLoader().getResource(NATIVE_FOLDER_NAME);
            if (resourceUrl != null) {
                return resourceUrl.toURI().getPath();
            }
        } catch (URISyntaxException e) {
            // TODO: make logs
            e.printStackTrace();
        }

        return null;
    }
}
