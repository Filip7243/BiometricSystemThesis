package com.bio.bio_backend.config;

import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.biometrics.NMatchingSpeed;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import static com.neurotec.biometrics.NMatchingSpeed.LOW;

@Configuration
public class SDKConfig {

    @PostConstruct
    public void init() {
        LibraryManager.initLibraryPath();
        LicenseManager.obtainLicense();

        FingersTools.getInstance().getClient().setFingersMatchingSpeed(LOW);
        FingersTools.getInstance().getClient().setMatchingThreshold(60);
    }
}
