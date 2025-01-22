package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.UserService;
import com.example.client.dto.FingerprintDTO;
import com.example.client.dto.UpdateFingerprintRequest;
import com.example.gui.ScannersListPanel;
import com.example.utils.EncryptionUtils;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JDialog fingerScanDialog;

    private NSubject subject;
    private Runnable onFingerprintUpdate;

    public FingerprintViewDialog(Frame parent, FingerprintDTO fingerprint, UserService userService, Runnable onFingerprintUpdate) throws Exception {
        super(parent, "Fingerprint Details", true);
        this.fingerprint = fingerprint;
        this.userService = userService;
        this.currentImageBytes = fingerprint.originalImage();
        this.onFingerprintUpdate = onFingerprintUpdate;

        initComponents();
    }

    private void initComponents() throws Exception {
        setLayout(new BorderLayout());
        setSize(700, 600);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Image display panel
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        updateImageDisplay();

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton rescanButton = new JButton("Rescan Fingerprint");
        styleButton(rescanButton, new Color(52, 152, 219), 200, 40);

        rescanButton.addActionListener(e -> performFingerScan());
        buttonPanel.add(rescanButton);

        add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Fingerprint Details", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray
        headerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headerDetails = new JLabel(
                "Fingerprint ID: " + fingerprint.id() + " | Finger Type: " + fingerprint.fingerType(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray
        headerDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(headerTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add some vertical spacing
        headerPanel.add(headerDetails);

        return headerPanel;
    }

    private void updateImageDisplay() throws Exception {
        try {
            if (currentImageBytes != null && currentImageBytes.length > 0) {
                byte[] decryptedImage = EncryptionUtils.decrypt(currentImageBytes);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(decryptedImage));
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
        fingerScanDialog = new JDialog();
        fingerScanDialog.setTitle("Fingerprint Scanning");
        fingerScanDialog.setModal(true);

        // Create Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Fingerprint Scanning", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray
        headerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headerDetails = new JLabel(
                "Capture fingerprint: " + fingerprint.fingerType(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray
        headerDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(headerTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add some vertical spacing
        headerPanel.add(headerDetails);

        // Existing scanner list panel
        ScannersListPanel scannersListPanel = new ScannersListPanel();
        scannersListPanel.updateScannerList();
        scannersListPanel.setPreferredSize(new Dimension(scannersListPanel.getWidth(), 80));

        NFingerView view = new NFingerView();

        // Style buttons using the existing styling function
        btnScan = new JButton("Scan");
        styleButton(btnScan, new Color(52, 152, 219), 120, 40);
        btnScan.addActionListener(e -> startCapturing(view));

        saveButton = new JButton("Save Changes");
        styleButton(saveButton, new Color(46, 204, 113), 150, 40);
        saveButton.addActionListener(e -> saveFingerprint());

        stopButton = new JButton("Stop");
        styleButton(stopButton, new Color(231, 76, 60), 120, 40);
        stopButton.addActionListener(e -> stopCapturing());

        JPanel fingerPanel = createFingerPanel(
                "Fingerprint",
                view,
                btnScan,
                saveButton,
                stopButton
        );

        fingerScanDialog.setLayout(new BorderLayout());
        fingerScanDialog.add(headerPanel, BorderLayout.NORTH);
        fingerScanDialog.add(scannersListPanel, BorderLayout.CENTER);
        fingerScanDialog.add(fingerPanel, BorderLayout.SOUTH);

        fingerScanDialog.setSize(800, 700);
        fingerScanDialog.setLocationRelativeTo(null);
        fingerScanDialog.setVisible(true);
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

        subject = new NSubject();
        subject.getFingers().add(finger);

        view.setFinger(finger);
        view.setShownImage(ORIGINAL);

        NBiometricTask task = FingersTools.getInstance()
                .getClient()
                .createTask(EnumSet.of(CAPTURE), subject);  // TODO: maybe to repair
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
        mainPanel.setPreferredSize(new Dimension(mainPanel.getPreferredSize().width, 400));

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
                    subject.getFingers().get(0).getImage().save().toByteArray()
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
                        onFingerprintUpdate.run();
                        fingerScanDialog.dispose();
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
                    System.out.println("SUCCESS");  // TODO: add here real handler
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

    private void styleButton(JButton button, Color backgroundColor, int width, int height) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
    }
}
