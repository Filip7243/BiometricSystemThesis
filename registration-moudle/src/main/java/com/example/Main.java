package com.example;

import com.example.gui.MainPanel;
import com.example.utils.LibraryManager;
import com.neurotec.licensing.NLicenseManager;
import com.neurotec.samples.util.Utils;

import javax.swing.*;

import static java.awt.BorderLayout.CENTER;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {
    public static void main(String[] args) {
        Utils.setupLookAndFeel();
        LibraryManager.initLibraryPath();

        NLicenseManager.setTrialMode(false);

        // TODO: Login panel based on fingerprint and clean code test db
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Admin Panel");
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.add(new MainPanel(), CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}