package com.bio.bio_backend.utils;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.io.NBuffer;
import com.neurotec.licensing.NLicense;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasa do zarządzania licencjami i klientami biometrycznymi.
 * Umożliwia uzyskanie licencji oraz zarządzanie klientami NBiometricClient.
 */
public final class FingersTools {

    private static FingersTools instance;

    private static final String ADDRESS = "/local"; // Adres serwera licencji
    private static final String PORT = "5000"; // Port serwera licencji

    /**
     * Pobiera instancję klasy FingersTools.
     * Używa wzorca projektowego Singleton, zapewniając jedną instancję tej klasy.
     *
     * @return instancja klasy FingersTools
     */
    public static FingersTools getInstance() {
        synchronized (FingersTools.class) {
            if (instance == null) {
                instance = new FingersTools();
            }
            return instance;
        }
    }

    private final Map<String, Boolean> licenses; // Mapa przechowująca status konkretnych licencji
    private final NBiometricClient client; // Główny klient biometryczny - rozszerzenie klasy NBiometricEngine
    private final NBiometricClient defaultClient; // Domyślny klient biometryczny


    /**
     * Konstruktor klasy FingersTools, inicjalizujący pola.
     */
    private FingersTools() {
        licenses = new HashMap<>();
        client = new NBiometricClient();
        defaultClient = new NBiometricClient();
    }

    /**
     * Sprawdza, czy licencja została już uzyskana.
     *
     * @param license nazwa licencji
     * @return true, jeśli licencja została uzyskana, w przeciwnym razie false
     */
    private boolean isLicenseObtained(String license) {
        if (license == null) throw new NullPointerException("license");
        return licenses.getOrDefault(license, false);
    }

    /**
     * Uzyskuje licencje na podstawie listy nazw licencji.
     *
     * @param names lista nazw licencji do uzyskania
     * @return true, jeśli wszystkie licencje zostały pomyślnie uzyskane, w przeciwnym razie false
     * @throws IOException jeśli wystąpi błąd przy odczycie pliku licencji
     */
    public boolean obtainLicenses(List<String> names) throws IOException {
        if (names == null) {
            return true;
        }
        boolean result = true;
        for (String license : names) {
            if (isLicenseObtained(license)) {
                System.out.println(license + ": " + " already obtained");
            } else {
                // Licencja pobierana jest z pliku (.lic) znajdującego się na maszynie
//                String licPath = "C:\\Users\\Filip\\Desktop\\STUDIA\\inzynierka\\MM_13.0\\" +
//                        "Neurotec_Biometric_13_0_SDK\\Bin\\Win64_x64\\Activation\\" +
//                        "Trial_PC__Valid-until_2025-06-04_5170789018306794686_internet_license.lic";
                String licPath = "/app/license.lic";

                // Odczytywanie pliku z licencją
                byte[] bytes = Files.readAllBytes(Paths.get(licPath));
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                NLicense.add(NBuffer.fromByteBuffer(byteBuffer));

                // Próba pobrania licencji
                boolean state = NLicense.obtainComponents(ADDRESS, PORT, license);
                licenses.put(license, state);
                if (state) {
                    System.out.println(license + ": obtained");
                } else {
                    result = false;
                    System.out.println(license + ": not obtained");
                }
            }
        }
        return result;
    }

    /**
     * Pobiera mapę przechowującą statusy licencji.
     *
     * @return mapa nazw licencji i ich statusów (true - uzyskana, false - nieuzyskana)
     */
    public Map<String, Boolean> getLicenses() {
        return licenses;
    }

    /**
     * Pobiera główny klient biometryczny.
     *
     * @return klient biometryczny
     */
    public NBiometricClient getClient() {
        return client;
    }

    /**
     * Pobiera domyślny klient biometryczny.
     *
     * @return domyślny klient biometryczny
     */
    public NBiometricClient getDefaultClient() {
        return defaultClient;
    }
}
