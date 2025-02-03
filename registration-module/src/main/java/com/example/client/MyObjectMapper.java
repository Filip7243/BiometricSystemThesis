package com.example.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Klasa do zarządzania instancją {@link ObjectMapper} korzystająca ze wzorca singleton.
 */
public class MyObjectMapper {
    private static volatile ObjectMapper instance;

    private MyObjectMapper() {
    }

    /**
     * Metoda zwracająca instancję {@link ObjectMapper}.
     * Metoda jest bezpieczna dla wątków (thread-safe) dzięki podwójnemu sprawdzaniu blokady (double-checked locking).
     *
     * @return Instancja {@link ObjectMapper} skonfigurowana do ignorowania nieznanych właściwości
     * i błędów związanych z brakującymi właściwościami w konstruktorze.
     */
    public static ObjectMapper getInstance() {
        if (instance == null) {
            // Synchronizacja dla bezpieczeństwa wielowątkowego
            synchronized (MyObjectMapper.class) {
                if (instance == null) {
                    instance = new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Ignorowanie nieznanych właściwości w JSONie
                            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false); // Ignorowanie błędów związanych z brakującymi właściwościami w konstruktorze
                }
            }
        }
        return instance;
    }
}