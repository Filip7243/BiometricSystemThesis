package com.example.gui;

import com.example.FingersTools;
import com.example.client.UserClient;
import com.example.client.UserService;
import com.example.client.dto.BiometricsLoginRequest;
import com.example.client.dto.PasswordLoginRequest;
import com.example.model.FingerType;
import com.example.utils.EncryptionUtils;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.example.gui.StyledComponentFactory.createStyledPasswordField;
import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricStatus.CANCELED;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class LoginPanel extends BasePanel {

    private NSubject subject;

    private final ScannersListPanel slp = new ScannersListPanel();
    private final NFingerView view = new NFingerView();
    private boolean isScanning = false;
    private final JButton scanBtn;
    private final JButton cancelBtn;
    private final CaptureHandler captureHandler = new CaptureHandler();
    private final UserService userService = new UserService(new UserClient());

    private FingerType fingerToScan;
    private JLabel subHeaderLabel;

    public LoginPanel() {
        super();

        scanBtn = createStyledButton("SCAN", new Color(52, 152, 219), 150, 40);
        cancelBtn = createStyledButton("CANCEL", new Color(231, 76, 60), 150, 40);

        fingerToScan = getRandomFinger();
        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");
    }

    @Override
    protected void initGUI() {
        try {
            obtainLicenses(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        updateFingersTools();
        slp.updateScannerList();

        setPreferredSize(new Dimension(800, 600));
        setLayout(new GridBagLayout()); // Dodanie GridBagLayout dla wyśrodkowania

        JTabbedPane tabbedPane = new JTabbedPane();

        // Zakładka logowania
        JPanel loginTab = createLoginTab();

        // Zakładka skanowania palca
        JPanel fingerprintTab = createFingerTab();

        // Dodanie zakładek do panelu
        tabbedPane.addTab("Login with biometrics", fingerprintTab);
        tabbedPane.addTab("Login with password", loginTab);

        // Dodanie zakładek do głównego okna i wyśrodkowanie
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER; // Wyśrodkowanie
        gbc.fill = GridBagConstraints.BOTH;    // Rozciąganie zakładek
        add(tabbedPane, gbc);
    }

    @Override
    protected void setDefaultValues() {

    }

    @Override
    protected void updateControls() {
        scanBtn.setEnabled(!isScanning);
        cancelBtn.setEnabled(isScanning);

        subHeaderLabel.setText("Finger to scan: " + fingerToScan);
    }

    @Override
    protected void updateFingersTools() {

    }

    private JPanel createLoginTab() {
        // Tworzenie głównego panelu
        JPanel loginPanel = new JPanel(new BorderLayout(10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding wokół panelu

        // Nagłówek
        JLabel headerLabel = new JLabel("Login with password", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(new Color(52, 73, 94));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0)); // Marginesy
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie
        loginPanel.add(headerLabel, BorderLayout.NORTH);

        // Panel centralny z układem GridBagLayout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label "Password"
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(passwordLabel, gbc);

        // Pole hasła
        JPasswordField passwordField = createStyledPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(passwordField, gbc);

        // Przycisk "Zaloguj"
        JButton loginButton = createStyledButton("Login", new Color(52, 152, 219), 150, 45);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(loginButton, gbc);

        // Label statusu logowania
        JLabel loginStatusLabel = new JLabel("", SwingConstants.CENTER);
        loginStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginStatusLabel.setForeground(Color.RED); // Domyślnie czerwony
        gbc.gridx = 1;
        gbc.gridy = 2;
        centerPanel.add(loginStatusLabel, gbc);

        loginPanel.add(centerPanel, BorderLayout.CENTER);
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Obsługa przycisku logowania
        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            try {
                byte[] encryptedPassword = EncryptionUtils.encrypt(password.getBytes());

                // send request to backend
                userService.loginToAdminPanelWithPassword(new PasswordLoginRequest(encryptedPassword),
                        (response) -> {
                            if (response.isLoggedIn()) {
                                loginStatusLabel.setText("Logged in successfully!");
                                loginStatusLabel.setForeground(new Color(34, 139, 34)); // Zielony kolor

                                SwingUtilities.invokeLater(() -> {
                                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(LoginPanel.this);
                                    frame.getContentPane().removeAll();
                                    frame.add(new MainPanel(), BorderLayout.CENTER);
                                    frame.revalidate();
                                    frame.repaint();
                                });
                            } else {
                                loginStatusLabel.setText("Invalid password!");
                                loginStatusLabel.setForeground(Color.RED);
                            }
                        },
                        getParent());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return loginPanel;
    }

    private JPanel createFingerTab() {
        // Tworzenie głównego panelu
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        // Tworzenie panelu na nagłówki i ScannersListPanel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(245, 245, 245));

        // Dodanie ScannersListPanel do topPanel na samej górze
        topPanel.add(slp);

        // Tworzenie panelu dla nagłówków
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 245, 245));

        // Nagłówek
        JLabel headerLabel = new JLabel("Login with your biometrics", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(52, 73, 94));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Marginesy
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie
        headerPanel.add(headerLabel);

        // Podtytuł
        String fingerToScanTxt = "Finger to scan: " + fingerToScan;
        subHeaderLabel = new JLabel(fingerToScanTxt, SwingConstants.CENTER);
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(100, 100, 100));
        subHeaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Marginesy
        subHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie
        headerPanel.add(subHeaderLabel);

        // Dodanie headerPanel do topPanel poniżej ScannersListPanel
        topPanel.add(headerPanel);

        // Dodanie topPanel (nagłówki + slp) do głównego panelu (na górze)
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tworzenie panelu do widoku Fingerprint (scrollPane)
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Fingerprint View",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        view.setShownImage(ORIGINAL);
        view.setAutofit(true);
        scrollPane.setViewportView(view);

        // Ustawienie rozmiaru FingerView (2/3 ekranu)
        scrollPane.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Umieszczenie na środku

        // Listener do FingerView
        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (view.getFinger() != null) {
                    view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                view.setCursor(Cursor.getDefaultCursor());
            }
        });

        scanBtn.addActionListener(e -> startCapturing());
        cancelBtn.addActionListener(e -> cancelCapturing());

        // Panel przycisków
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBackground(new Color(245, 245, 245));

        // Układ dla przycisków
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 20, 10, 20);
        btnPanel.add(scanBtn, gbc);

        gbc.gridx = 1;
        btnPanel.add(cancelBtn, gbc);

        // Dodanie panelu przycisków na dole
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    public static FingerType getRandomFinger() {
        FingerType[] values = FingerType.values();
        Random random = new Random();
        int randomIndex = random.nextInt(values.length - 1); // -1, żeby pominąć NONE
        return values[randomIndex];
    }

    private void startCapturing() {
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
                .createTask(EnumSet.of(CAPTURE), subject);
        FingersTools.getInstance().getClient().performTask(task, null, captureHandler);

        isScanning = true;
        updateControls();
    }

    private void cancelCapturing() {
        FingersTools.getInstance().getClient().cancel();

        isScanning = false;
        updateControls();
    }

    public void obtainLicenses(BasePanel panel) throws IOException {
        if (!panel.isObtained()) {
            boolean status = FingersTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
            FingersTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
        }
    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                isScanning = false;

                if (result.getStatus() == OK) {
                    byte[] file = subject.getFingers()
                            .get(0)
                            .getImage()
                            .save()
                            .toByteArray();

                    try {
                        byte[] encryptedFile = EncryptionUtils.encrypt(file);

                        BiometricsLoginRequest request = new BiometricsLoginRequest(encryptedFile, fingerToScan);

                        userService.loginToAdminPanelWithBiometrics(request,
                                (response) -> {
                                    if (response.isLoggedIn()) {
                                        SwingUtilities.invokeLater(() -> {
                                            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(LoginPanel.this);
                                            frame.getContentPane().removeAll();
                                            frame.add(new MainPanel(), BorderLayout.CENTER);
                                            frame.revalidate();
                                            frame.repaint();
                                        });
                                    } else {
                                        showError("Login failed");
                                    }
                                },
                                getParent());

                        fingerToScan = getRandomFinger();
                    } catch (Exception e) {
                        updateControls();
                        throw new RuntimeException("Something went wrong when encrypting: " + e.getMessage());
                    }
                } else if (result.getStatus() == CANCELED) {
                    System.out.println("Canceled");
                } else {
                    showError("Something went wrong: " + result.getStatus().toString());
                }

                updateControls();
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                isScanning = false;
                showError(throwable);
                updateControls();
            });
        }
    }
}
