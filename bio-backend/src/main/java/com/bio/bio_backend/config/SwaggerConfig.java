package com.bio.bio_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bio Backend API",
                version = "1.0",
                description = "Dokumentacja API dla aplikacji Bio Backend"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Lokalny serwer deweloperski")
        }
)
public class SwaggerConfig {
}
