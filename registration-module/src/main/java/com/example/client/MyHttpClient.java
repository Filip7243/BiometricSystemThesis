package com.example.client;

import java.net.http.HttpClient;

/**
 * Klasa do zarządzania instancją {@link HttpClient} korzystająca ze wzorca singleton.
 */
public final class MyHttpClient {
    private static volatile HttpClient instance;

    private MyHttpClient() {
    }

    /**
     * Metoda zwracająca instancję {@link HttpClient}.
     * Metoda jest bezpieczna dla wątków (thread-safe) dzięki podwójnemu sprawdzaniu blokady (double-checked locking).
     *
     * @return Instancja {@link HttpClient} skonfigurowana do użycia HTTP/2.
     */
    public static HttpClient getInstance() {
        if (instance == null) {
            // Synchronizacja dla bezpieczeństwa wielowątkowego
            synchronized (MyHttpClient.class) {
                if (instance == null) {
                    instance = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2) // Ustawienie wersji HTTP/2
                            .build();
                }
            }
        }
        return instance;
    }
}
