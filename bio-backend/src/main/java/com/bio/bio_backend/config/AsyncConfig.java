package com.bio.bio_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Klasa konfiguracyjna dla obsługi asynchronicznych operacji w aplikacji.
 * <p>
 * Ta klasa umożliwia korzystanie z funkcjonalności asynchronicznych w aplikacji, dzięki
 * adnotacji {@link EnableAsync}. Umożliwia to wykonywanie metod w tle, nie blokując głównego wątku aplikacji.
 * <p>
 * Adnotacja {@link EnableAsync} włącza wsparcie dla asynchronicznych metod w aplikacji,
 * umożliwiając użycie adnotacji {@link Async} w metodach serwisów, które mają działać asynchronicznie.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
