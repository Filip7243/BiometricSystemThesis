package com.bio.bio_backend.config;

import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.licensing.NLicenseManager;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

/**
 * Klasa odpowiedzialna za zarządzanie licencjami wymaganymi do działania aplikacji.
 * Uzyskuje licencje niezbędne do korzystania z określonych komponentów.
 */
@Configuration
public class LicenseManager {

    /**
     * Uzyskuje licencje wymagane do działania aplikacji.
     * Licencje obejmują m.in. ekstrakcję danych biometrycznych, dopasowywanie odcisków
     * palców, obsługę skanerów palców oraz obsługę obrazów WSQ.
     */
    static void obtainLicense() {
        try {
            boolean status = FingersTools.getInstance()
                    .obtainLicenses(
                            List.of("Biometrics.FingerExtraction", // Ekstrakcja danych z odcisków palców
                                    "Biometrics.FingerExtractionBase",
                                    "Devices.FingerScanners", // Obsługa skanerów linii papilarnych
                                    "Biometrics.FingerMatching", // Dopasowywanie odcisków palców (identyfikacja/weryfikacja)
                                    "Images.WSQ" // Obsługa obrazów WSQ
                            )
                    );

            System.out.println("LICENSES OBTAINED: " + status);
        } catch (IOException e) {
            System.out.println("EXCEPTION WHEN OBTAINING LICENSES");
        }
    }
}
