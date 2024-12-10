package com.bio.bio_backend.config;

import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.licensing.NLicenseManager;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class LicenseManager {

    static void obtainLicense() {
        try {
            boolean status = FingersTools.getInstance()
                    .obtainLicenses(
                            List.of("Biometrics.FingerExtraction",
                                    "Biometrics.FingerExtractionBase",
                                    "Devices.FingerScanners",
                                    "Biometrics.FingerMatching",
                                    "Images.WSQ"
                            )
                    );

            System.out.println("LICENSES OBTAINED: " + status);
        } catch (IOException e) {
            System.out.println("EXCEPTION WHEN OBTAINING LICENSES");
        }
    }
}
