package com.example;

import com.example.client.UserClient;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.client.request.UserCreationRequest;
import com.example.gui.MainPanel;
import com.example.model.FingerType;
import com.example.model.Fingerprint;
import com.example.model.Role;
import com.example.utils.EncryptionUtils;
import com.example.utils.LibraryManager;
import com.neurotec.licensing.NLicenseManager;
import com.neurotec.samples.util.Utils;

import javax.swing.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.BorderLayout.CENTER;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) throws IOException {
        Utils.setupLookAndFeel();
        LibraryManager.initLibraryPath();

        NLicenseManager.setTrialMode(false);

        // TODO: Login panel based on fingerprint and clean code test db refresh btn to assign rooms
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Admin Panel");
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.add(new MainPanel(), CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

//        saveUser();
    }


    private static void saveUser() throws IOException {
        Map<FingerType, byte[]> encryptedFingerprintData = new HashMap<>();

        Path thumb = Paths.get("C:\\Users\\Filip\\Downloads\\pres\\filip-thumb.jpg");
        Path index = Paths.get("C:\\Users\\Filip\\Downloads\\pres\\filip-index.jpg");
        Path middle = Paths.get("C:\\Users\\Filip\\Downloads\\pres\\filip-middle.jpg");

        byte[] thumbBytes = Files.readAllBytes(thumb);
        byte[] indexBytes = Files.readAllBytes(index);
        byte[] middleBytes = Files.readAllBytes(middle);

        HashMap<FingerType, byte[]> fingerprints = new HashMap<>();
        fingerprints.put(FingerType.THUMB, thumbBytes);
        fingerprints.put(FingerType.INDEX, indexBytes);
        fingerprints.put(FingerType.MIDDLE, middleBytes);

        for (Map.Entry<FingerType, byte[]> entry : fingerprints.entrySet()) {
            FingerType finger = entry.getKey();
            byte[] fingerprint = entry.getValue();

            try {
                byte[] encryptedImage = EncryptionUtils.encrypt(fingerprint);
                encryptedFingerprintData.put(finger, encryptedImage);
            } catch (Exception e) {
                return;
            }
        }

        UserCreationRequest request = new UserCreationRequest(
                "John",
                "Doe",
                "08543728192",
                Role.ADMIN,
                encryptedFingerprintData,  // Now using encrypted data
                List.of(1L, 2L)
        );

        new UserClient().createUser(request);

//        setDefaultValues();

//        showMessageDialog(this, "User created successfully!", "Success", PLAIN_MESSAGE);
    }
}