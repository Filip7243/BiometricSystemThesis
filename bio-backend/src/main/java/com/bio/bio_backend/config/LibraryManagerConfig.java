package com.bio.bio_backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LibraryManagerConfig {

    @PostConstruct
    public void init() {
        LibraryManager.initLibraryPath();
    }
}
