package com.example.gui;

import com.example.FingersTools;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricOperation.CREATE_TEMPLATE;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public final class RegisterUserTab extends BasePanel implements ActionListener, ItemListener {

    private final CaptureHandler captureHandler = new CaptureHandler();
    private final List<NSubject> scannedFingers;

    private NFingerView view;
    private JButton btnScan;
    private JButton btnCancelScan;
    private JButton btnSubmitForm;
    private JButton btnAddRooms;
    private JButton btnRemoveRooms;
    private JButton btnRefreshLists;
    private JLabel lblInfo;
    private NSubject subject;

    private boolean scanning;

    private PersonalDataFormPanel personalDataFormPanel;
    private FingerViewPanel fingerViewPanel;

    public RegisterUserTab() {
        super();

        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");

        scannedFingers = new ArrayList<>();
    }

    void updateStatus(String status) {
        lblInfo.setText(status);
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(panelLicensing, NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, CENTER);

        ScannersListPanel scannersListPanel = new ScannersListPanel();

        personalDataFormPanel = new PersonalDataFormPanel(new DocumentListenerImpl());

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(scannersListPanel, NORTH);
        northPanel.add(personalDataFormPanel, CENTER);

        mainPanel.add(northPanel, NORTH);

        BuildingRoomsPanel buildingRoomsPanel = new BuildingRoomsPanel();
        fingerViewPanel = new FingerViewPanel();

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buildingRoomsPanel, NORTH);
        centerPanel.add(fingerViewPanel, CENTER);

        mainPanel.add(centerPanel, CENTER);

        view = fingerViewPanel.getFingerView();
        btnScan = fingerViewPanel.getBtnScan();
        btnCancelScan = fingerViewPanel.getBtnCancel();
        lblInfo = fingerViewPanel.getLblInfo();

        btnAddRooms = buildingRoomsPanel.getBtnAddRooms();
        btnRemoveRooms = buildingRoomsPanel.getBtnRemoveRooms();
        btnRefreshLists = buildingRoomsPanel.getBtnRefreshLists();

        btnSubmitForm = new JButton("Submit Form");
        btnSubmitForm.setPreferredSize(new Dimension(btnSubmitForm.getPreferredSize().width, 50));

        btnScan.addActionListener(this);
        btnCancelScan.addActionListener(this);
        btnSubmitForm.addActionListener(this);

        mainPanel.add(btnSubmitForm, SOUTH);
    }

    @Override
    protected void setDefaultValues() {
        // No default values
    }

    @Override
    protected void updateControls() {
        btnScan.setEnabled(!scanning);
        btnCancelScan.setEnabled(scanning);

        btnRefreshLists.setEnabled(!scanning);
        btnAddRooms.setEnabled(!scanning);
        btnRemoveRooms.setEnabled(!scanning);

        btnSubmitForm.setEnabled(!scanning && subject != null && subject.getStatus() == OK && scannedFingers.size() == 3);
    }

    private void startCapturing() {
        updateStatus("Starting capturing...");

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
                .createTask(EnumSet.of(CAPTURE, CREATE_TEMPLATE), subject);
        FingersTools.getInstance().getClient().performTask(task, null, captureHandler);

        scanning = true;
        updateControls();
    }

    private void cancelCapturing() {
        FingersTools.getInstance().getClient().cancel();

        scanning = false;
        updateControls();
    }

    private void saveToDatabase() {
        scannedFingers.clear();
        updateControls();
    }

    @Override
    protected void updateFingersTools() {
        FingersTools.getInstance().getClient().reset();
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        FingersTools.getInstance().getClient().setFingersReturnBinarizedImage(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(btnScan)) {
            startCapturing();
        } else if (source.equals(btnCancelScan)) {
            cancelCapturing();
        } else if (source.equals(btnSubmitForm)) {
            saveToDatabase();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                scanning = false;

                fingerViewPanel.updateShownImage();
                if (result.getStatus() == OK) {
                    updateStatus("Quality: " + subject.getFingers().get(0).getObjects().get(0).getQuality());
                    if (scannedFingers.size() < 3) {
                        scannedFingers.add(subject);
                    }
                } else {
                    updateStatus("Failed to capture. " + result.getStatus());
                }

                updateControls();
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                scanning = false;
                fingerViewPanel.updateShownImage();
                showError(throwable);
                updateControls();
            });
        }
    }

    private final class DocumentListenerImpl implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateButtonState();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateButtonState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateButtonState();
        }

        private void updateButtonState() {
            boolean enableButton = !personalDataFormPanel.getFirstName().isEmpty() &&
                    !personalDataFormPanel.getLastName().isEmpty() &&
                    !personalDataFormPanel.getPesel().isEmpty() &&
                    personalDataFormPanel.getCmbRoles().getSelectedItem() != null;

            btnScan.setEnabled(enableButton);
        }
    }
}
