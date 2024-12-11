package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.UserService;
import com.example.client.dto.FingerprintDTO;
import com.example.client.dto.UpdateFingerprintRequest;
import com.example.gui.ScannersListPanel;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumSet;

import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricOperation.CREATE_TEMPLATE;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.Color.BLACK;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class FingerprintViewDialog extends JDialog {
    private final UserService userService;
    private final FingerprintDTO fingerprint;
    private byte[] currentImageBytes;
    private JLabel imageLabel;
    private JButton saveButton;
    private JButton btnScan;
    private JButton stopButton;

    public FingerprintViewDialog(Frame parent, FingerprintDTO fingerprint, UserService userService) {
        super(parent, "Fingerprint Details", true);
        this.fingerprint = fingerprint;
        this.userService = userService;
        this.currentImageBytes = fingerprint.originalImage();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(null);

        // Image display panel
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        updateImageDisplay();

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton rescanButton = new JButton("Rescan Fingerprint");

        rescanButton.addActionListener(e -> performFingerScan());

        buttonPanel.add(rescanButton);

        add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void updateImageDisplay() {
        try {  // TODO: view image properly, so download from db etc. cache in table and remove on exit
            if (currentImageBytes != null && currentImageBytes.length > 0) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(currentImageBytes));

                ImageIcon icon = new ImageIcon(image.getScaledInstance(400, 400, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
            } else {
                imageLabel.setText("No fingerprint image available");
                imageLabel.setIcon(null);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error displaying fingerprint: " + e.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void performFingerScan() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Available Scanners");
        dialog.setModal(true);

        ScannersListPanel scannersListPanel = new ScannersListPanel();
        scannersListPanel.updateScannerList();

        NFingerView view = new NFingerView();

        btnScan = new JButton("Scan");
        btnScan.addActionListener(e -> startCapturing(view));

        saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveFingerprint());

        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopCapturing());

        JPanel fingerPanel = createFingerPanel(
                "Fingerprint",
                view,
                btnScan,
                saveButton,
                stopButton
        );

        dialog.setLayout(new BorderLayout());
        dialog.add(scannersListPanel, BorderLayout.NORTH);
        dialog.add(fingerPanel, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void startCapturing(NFingerView view) {
        FingersTools.getInstance().getClient().clear();

        saveButton.setEnabled(false);
        btnScan.setEnabled(false);
        stopButton.setEnabled(true);

        if (FingersTools.getInstance().getClient().getFingerScanner() == null) {
            SwingUtilities.invokeLater(() -> showMessageDialog(
                    this,
                    "Please select scanner from the list.",
                    "No scanner selected",
                    PLAIN_MESSAGE)
            );
        }

        NFinger finger = new NFinger();

        NSubject subject = new NSubject();
        subject.getFingers().add(finger);

        view.setFinger(finger);
        view.setShownImage(ORIGINAL);

        NBiometricTask task = FingersTools.getInstance()
                .getClient()
                .createTask(EnumSet.of(CAPTURE, CREATE_TEMPLATE), subject);
        FingersTools.getInstance().getClient().performTask(task, null, new CaptureHandler());
    }

    private void stopCapturing() {
        FingersTools.getInstance().getClient().cancel();

        saveButton.setEnabled(false);
        btnScan.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private JPanel createFingerPanel(String title,
                                     NFingerView view,
                                     JButton scanBtn,
                                     JButton saveBtn,
                                     JButton stopBtn) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(createLineBorder(BLACK), title));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        view.setShownImage(ORIGINAL);
        view.setAutofit(true);
        scrollPane.setViewportView(view);

        saveBtn.setEnabled(false);
        stopButton.setEnabled(false);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(scanBtn, BorderLayout.WEST);
        btnPanel.add(saveBtn, BorderLayout.EAST);
        btnPanel.add(stopBtn, BorderLayout.CENTER);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void saveFingerprint() {
        try {
            UpdateFingerprintRequest updatedFingerprint = new UpdateFingerprintRequest(
                    fingerprint.id(),
                    fingerprint.token()
            );

            System.out.println("Updating fingerprint: " + updatedFingerprint);

            userService.updateUserFingerprint(
                    updatedFingerprint,
                    (result) -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "Fingerprint updated successfully",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        dispose();
                    },
                    this
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error preparing fingerprint update: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                if (result.getStatus() == OK) {
                    System.out.println("SUCCESS");  // tODO: add here real handler
                    saveButton.setEnabled(true);
                    btnScan.setEnabled(true);
                    stopButton.setEnabled(false);
                } else {
                    System.out.println("FAILED TO CAPTURE FINGERPRINT");
                }
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                System.out.println("FAILED TO CAPTURE FINGERPRINT in failed!!!");
            });
        }
    }
}
