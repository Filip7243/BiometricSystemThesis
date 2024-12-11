package com.bio.bio_backend;

import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.licensing.NLicenseManager;
import com.neurotec.plugins.NPluginManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.EnumSet;

import static com.neurotec.devices.NDeviceType.FINGER_SCANNER;

@SpringBootApplication
public class BioBackendApplication {

    public static void main(String[] args) {
        NLicenseManager.setTrialMode(false);

        SpringApplication.run(BioBackendApplication.class, args);
    }
}
