package com.example.gui;

import com.example.FingersTools;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NFingerScanner;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
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
import static com.neurotec.devices.NDeviceType.FINGER_SCANNER;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class RegisterUserTab extends BasePanel implements ActionListener, ItemListener {

    private final NDeviceManager deviceManager;
    private final CaptureHandler captureHandler = new CaptureHandler();

    private NSubject subject;
    private NFingerView view;
    private JPanel mainPanel;
    private JPanel scannersPanel;
    private JScrollPane scrollPaneList;
    private JPanel inputsPanel;
    private JButton btnScan;
    private JButton btnCancelScan;
    private JButton btnSubmitForm;
    private JButton btnRefreshLists;
    private JLabel lblInfo;
    private JList<NDevice> scannerList;
    private JList<String> buildingList;
    private JList<String> roomList;
    private List<NSubject> scannedFingers;  // TODO: !!!ważne, żeby ją czyść bo zapisaniu do bazy danych!!!
    private boolean scanning;


    public RegisterUserTab() {
        super();

        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");

        this.scannedFingers = new ArrayList<>();

        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        this.deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        this.deviceManager.setDeviceTypes(EnumSet.of(FINGER_SCANNER));
        this.deviceManager.initialize();
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
        // TODO: request to save scannedFingers to database

        scannedFingers.clear();
        updateControls();
    }

    private void updateShownImage() {
        view.setShownImage(ORIGINAL);
    }

    private void updateScannerList() {
        DefaultListModel<NDevice> model = (DefaultListModel<NDevice>) scannerList.getModel();
        model.clear();

        for (NDevice device : deviceManager.getDevices()) {
            model.addElement(device);
        }

        NFingerScanner scanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if ((scanner == null) && (model.getSize() > 0)) {
            scannerList.setSelectedIndex(0);
        } else if (scanner != null) {
            scannerList.setSelectedValue(scanner, true);
        }
    }

    private void updateBuildingList() {
        DefaultListModel<String> model = (DefaultListModel<String>) buildingList.getModel();
        model.clear();
        // TODO: MAKE REQUEST TO GET BUILDINGS
    }

    private void updateRoomList() {
        DefaultListModel<String> model = (DefaultListModel<String>) roomList.getModel();
        model.clear();
        // TODO: MAKE REQUEST TO GET ROOMS
    }

    NSubject getSubject() {
        return subject;
    }

    NFingerScanner getSelectedScanner() {
        return (NFingerScanner) scannerList.getSelectedValue();
    }

    void updateStatus(String status) {
        this.lblInfo.setText(status);
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        // LICENSING PANEL - MAYBE TO REMOVE
        this.panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(this.panelLicensing, NORTH);

        this.mainPanel = new JPanel(new BorderLayout());
        add(this.mainPanel, CENTER);

        // SCANNERS PANEL
        this.scannersPanel = new JPanel(new BorderLayout());
        this.scannersPanel.setBorder(createTitledBorder("Scanners list"));
        this.mainPanel.add(this.scannersPanel, NORTH);

        // SCANNERS LIST
        this.scrollPaneList = new JScrollPane(this.scannerList = new JList<>());
        this.scannerList.setSelectionMode(SINGLE_SELECTION);
        this.scannerList.addListSelectionListener(null); // TODO: add listener to scanner lists
        this.scannersPanel.add(this.scrollPaneList, NORTH);

        // BUTTONS PANEL
        this.btnSubmitForm = new JButton("Submit form");
        this.btnSubmitForm.addActionListener(this);
        this.btnScan = new JButton("Scan");
        this.btnScan.addActionListener(this);
        this.btnCancelScan = new JButton("Cancel scan");
        this.btnCancelScan.addActionListener(this);
        this.btnRefreshLists = new JButton("Refresh lists");
        this.btnRefreshLists.addActionListener(this);

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

        btnSubmitForm.setEnabled(!scanning && subject != null && subject.getStatus() == OK && scannedFingers.size() == 3);
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
        } else if (source.equals(btnRefreshLists)) {
            updateScannerList();
            updateBuildingList();
            updateRoomList();
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

                updateShownImage();
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
                updateShownImage();
                showError(throwable);
                updateControls();
            });
        }
    }
}
