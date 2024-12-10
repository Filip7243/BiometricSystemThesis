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

        for (NPluginManager instance : NPluginManager.getInstances()) {
            System.out.println("Plugin manager: " + instance);
        }

        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        NDeviceManager deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(FINGER_SCANNER));
        deviceManager.initialize();
        for (NDevice device : deviceManager.getDevices()) {
            System.out.println("Device Name: " + device.getDisplayName());
            System.out.println("ID: " + device.getId());
            System.out.println("Manufacturer: " + device.getParent());
            System.out.println("Model: " + device.getModel());
            System.out.println("Serial Number: " + device.getSerialNumber());
            System.out.println("Is Plugged In: " + device.isAvailable());
        }
    }
}
