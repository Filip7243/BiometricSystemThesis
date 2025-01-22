package com.bio.bio_backend.config;

import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.biometrics.NMatchingSpeed;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import static com.neurotec.biometrics.NMatchingSpeed.LOW;

/**
 * Klasa konfiguracyjna odpowiedzialna za inicjalizację ustawień SDK.
 * Ustawia ścieżkę do bibliotek, uzyskuje licencje oraz konfiguruje parametry klienta biometrycznego.
 */
@Configuration
public class SDKConfig {

    /**
     * Metoda inicjalizująca ustawienia aplikacji po zainicjowaniu beana.
     * Ustawia ścieżkę do bibliotek, uzyskuje licencje oraz konfiguruje klienta biometrycznego.
     */
    @PostConstruct
    public void init() {
        // Inicjalizacja ścieżki do bibliotek natywnych
        LibraryManager.initLibraryPath();

        // Uzyskanie wymaganych licencji do działania SDK
        LicenseManager.obtainLicense();

        // Ustawienie parametrów dla klienta biometrycznego
        // Określenie szybkości dopasowywania odcisków palców (niska szybkość)
        FingersTools.getInstance().getClient().setFingersMatchingSpeed(LOW);

        // Ustawienie progu dopasowania dla klienta biometrycznego (60)
        FingersTools.getInstance().getClient().setMatchingThreshold(60);
    }
}
