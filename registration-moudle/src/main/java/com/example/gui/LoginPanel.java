package com.example.gui;

import com.example.FingersTools;
import com.example.client.UserClient;
import com.example.client.UserService;
import com.example.client.dto.FingerprintDTO;
import com.example.model.FingerType;
import com.example.model.Fingerprint;
import com.example.model.Role;
import com.example.model.User;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.io.NBuffer;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricOperation.CREATE_TEMPLATE;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static com.neurotec.devices.NDeviceType.FINGER_SCANNER;

public class LoginPanel extends JPanel {

    private NSubject subject;
    private Runnable onAuthenticationDone;
    private NDeviceManager deviceManager;
    private FingerType fingerToScan;

    private JLabel welcomeLabel;
    private final UserService userService = new UserService(new UserClient());

    public LoginPanel(Runnable onAuthenticationDone) {
        FingersTools.getInstance().getClient().reset();
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        FingersTools.getInstance().getClient().setFingersReturnBinarizedImage(true);

        this.onAuthenticationDone = onAuthenticationDone;

        deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(FINGER_SCANNER));
        deviceManager.initialize();

        NFingerScanner scanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if (scanner == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No fingerprint scanner found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel loginLabel = new JLabel("Login to admin panel");
        loginLabel.setAlignmentX(CENTER_ALIGNMENT);
        fingerToScan = getRandomFinger();
        JLabel fingerprintLabel = new JLabel("Place your " + fingerToScan.name() + " finger on the scanner");
        fingerprintLabel.setAlignmentX(CENTER_ALIGNMENT);
        welcomeLabel = new JLabel();
        welcomeLabel.setAlignmentX(CENTER_ALIGNMENT);

        NFingerView view = new NFingerView();
        NFinger finger = new NFinger();
        subject = new NSubject();
        subject.getFingers().add(finger);
        view.setFinger(finger);
        view.setShownImage(ORIGINAL);

        add(Box.createVerticalGlue());
        add(loginLabel);
        add(Box.createVerticalStrut(10));
        add(fingerprintLabel);
        add(Box.createVerticalStrut(10));
        add(view);
        add(Box.createVerticalStrut(20));
        add(welcomeLabel);
        add(Box.createVerticalGlue());

        NBiometricTask task = FingersTools.getInstance()
                .getClient()
                .createTask(EnumSet.of(CAPTURE, CREATE_TEMPLATE), subject);
        FingersTools.getInstance().getClient().performTask(task, null, new CaptureHandler());
    }

    public static FingerType getRandomFinger() {
        FingerType[] values = FingerType.values();
        Random random = new Random();
        int randomIndex = random.nextInt(values.length);
        return values[randomIndex];
    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                if (result.getStatus() == NBiometricStatus.OK) {
                    // Example: Handle the authentication logic
                    userService.getFingerprintsByTypeAndUserRole(
                            fingerToScan,
                            Role.ADMIN,
                            (fingerprints) -> {
                                NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

                                for (FingerprintDTO fingerprint : fingerprints) {
                                    NBuffer buffer = new NBuffer(fingerprint.token());
                                    NSubject subjectFromDB = NSubject.fromMemory(buffer);
                                    Long id = fingerprint.userId();
                                    subjectFromDB.setId(id.toString());
                                    enrollTask.getSubjects().add(subjectFromDB);
                                }

                                FingersTools.getInstance().getClient().performTask(enrollTask);
                                NBiometricStatus enrollStatus = enrollTask.getStatus();
                                if (enrollStatus == NBiometricStatus.OK) {
                                    NBiometricStatus identifyStatus = FingersTools.getInstance().getClient().identify(subject);

                                    if (identifyStatus == NBiometricStatus.OK) {
                                        for (NMatchingResult matchingResult : subject.getMatchingResults()) {
                                            System.out.format("Matched with ID: '%s' with score %d\n",
                                                    matchingResult.getId(), matchingResult.getScore());
                                        }
                                    } else {
                                        System.out.format("Identification failed. Status: %s\n", identifyStatus);
                                        FingersTools.getInstance().getClient().clear();
                                        onAuthenticationDone.run();
                                    }
                                }
                                if (!subject.getMatchingResults().isEmpty()) {
                                    NMatchingResult matchingResult = subject.getMatchingResults()
                                            .stream()
                                            .max(Comparator.comparing(NMatchingResult::getScore))
                                            .orElseThrow(() -> new RuntimeException("No matching results"));

                                    System.out.println("MATCHING RESULT " + matchingResult);

                                    userService.getUserById(Long.parseLong(subject.getId()), (user) -> {
                                        welcomeLabel.setText("Welcome " + user.firstName() + " " + user.lastName());
                                        FingersTools.getInstance().getClient().clear();
                                        onAuthenticationDone.run();
                                    }, null);

                                    FingersTools.getInstance().getClient().clear();
                                    onAuthenticationDone.run();
                                }
                            }, null);
                }
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                FingersTools.getInstance().getClient().clear();
                JOptionPane.showMessageDialog(
                        LoginPanel.this,
                        throwable.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}
