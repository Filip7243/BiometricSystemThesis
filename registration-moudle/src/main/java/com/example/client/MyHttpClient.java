package com.example.client;

import java.net.http.HttpClient;

public final class MyHttpClient {
    private static volatile HttpClient instance;

    private MyHttpClient() {
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (MyHttpClient.class) {
                if (instance == null) {
                    instance = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
