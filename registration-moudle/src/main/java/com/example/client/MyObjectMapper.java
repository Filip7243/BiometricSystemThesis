package com.example.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyObjectMapper {
    private static volatile ObjectMapper instance;

    private MyObjectMapper() {
    }

    public static ObjectMapper getInstance() {
        if (instance == null) {
            synchronized (MyObjectMapper.class) {
                if (instance == null) {
                    instance = new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                }
            }
        }
        return instance;
    }
}