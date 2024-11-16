package com.example;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.licensing.NLicense;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FingersTools {

    private static FingersTools instance;

    private final Map<String, Boolean> licenses;
    private final NBiometricClient client;
    private final NBiometricClient defaultClient;

    private static final String ADDRESS = "/local";
    private static final String PORT = "5000";

    private FingersTools() {
        licenses = new HashMap<>();
        client = new NBiometricClient();
        defaultClient = new NBiometricClient();
    }

    public static FingersTools getInstance() {
        synchronized (FingersTools.class) {
            if (instance == null) {
                instance = new FingersTools();
            }
            return instance;
        }
    }

    public boolean obtainLicenses(List<String> names) throws IOException {
        if (names == null) {
            return true;
        }
        boolean result = true;
        for (String license : names) {
            if (isLicenseObtained(license)) {
                System.out.println(license + ": " + " already obtained");
            } else {
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

    public Map<String, Boolean> getLicenses() {
        return licenses;
    }

    public NBiometricClient getClient() {
        return client;
    }

    public NBiometricClient getDefaultClient() {
        return defaultClient;
    }

    private boolean isLicenseObtained(String license) {
        if (license == null) throw new NullPointerException("license");
        return licenses.getOrDefault(license, false);
    }
}
