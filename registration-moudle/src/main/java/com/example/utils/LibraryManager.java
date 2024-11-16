package com.example.utils;

import java.net.URISyntaxException;
import java.net.URL;

public final class LibraryManager {
    private final static String LIBS_FOLDER_NAME = "libs";

    public static void initLibraryPath() {
        String libraryPath = getLibraryPath();
    }

    private static String getLibraryPath() {
        try {
            URL resourceUrl = LibraryManager.class.getClassLoader().getResource(LIBS_FOLDER_NAME);
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
