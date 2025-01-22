package com.bio.bio_backend.config;

import java.net.URISyntaxException;
import java.net.URL;

import static com.neurotec.samples.util.Utils.PATH_SEPARATOR;
import static com.neurotec.samples.util.Utils.isNullOrEmpty;
import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * Klasa odpowiedzialna za zarządzanie ścieżkami bibliotek natywnych.
 * Ustawia odpowiednie właściwości systemowe dla bibliotek natywnych oprogramowania Mega Matcher
 * używanych przez aplikację.
 */
public final class LibraryManager {
    private final static String NATIVE_FOLDER_NAME = "native"; // Nazwa folderu z bibliotekami natywnymi

    /**
     * Inicjalizuje ścieżki do bibliotek natywnych w systemie.
     * Ustawia właściwości systemowe potrzebne do załadowania bibliotek.
     */
    public static void initLibraryPath() {
        String libraryPath = getLibraryPath(); // Pobiera ścieżkę do folderu z bibliotekami
        String jnaLibraryPath = getProperty("jna.library.path"); // Pobiera istniejącą ścieżkę JNA

        // Jeśli ścieżka do bibliotek jest pusta, kończymy działanie metody
        if (isNullOrEmpty(libraryPath)) {
            return;
        }

        // Jeśli nie ma ustawionej ścieżki JNA, ustawiamy ją na ścieżkę do bibliotek
        if (isNullOrEmpty(jnaLibraryPath)) {
            System.setProperty("jna.library.path", libraryPath);
        } else {
            // Łączymy istniejącą ścieżkę JNA z nową ścieżką do bibliotek
            System.setProperty("jna.library.path", format("%s%s%s", jnaLibraryPath, PATH_SEPARATOR, libraryPath));
        }

        // Ustawiamy właściwość java.library.path, aby załadować biblioteki natywne
        System.setProperty("java.library.path", format("%s%s%s", getProperty("java.library.path"), PATH_SEPARATOR, libraryPath));
    }

    /**
     * Pobiera ścieżkę do folderu zawierającego biblioteki natywne.
     *
     * @return ścieżka do folderu z bibliotekami, lub null, jeśli nie znaleziono
     */
    private static String getLibraryPath() {
        try {
            // Próba pobrania URL do zasobu zawierającego biblioteki natywne
            URL resourceUrl = LibraryManager.class.getClassLoader().getResource(NATIVE_FOLDER_NAME);
            if (resourceUrl != null) {
                return resourceUrl.toURI().getPath(); // Zwraca ścieżkę do zasobu
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null; // Jeśli nie znaleziono zasobu, zwracamy null
    }
}
