package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.BuildingClient;
import com.example.client.UserClient;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.client.request.UserCreationRequest;
import com.example.gui.BasePanel;
import com.example.gui.LicensingPanel;
import com.example.gui.ScannersListPanel;
import com.example.model.*;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.utils.FingerProcessor.getFingerTemplate;
import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricOperation.CREATE_TEMPLATE;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class AddUserTab extends BasePanel implements ActionListener {

    private final CaptureHandler captureHandler = new CaptureHandler();
    private NSubject subject;
    private final List<Fingerprint> scannedFingers = new ArrayList<>();
    private NFingerView currentCapturingView;
    private FingerType currentFingerCapturing = FingerType.NONE;

    private LicensingPanel panelLicensing;  // TODO: add to initGUI
    private UserInputForm userInputForm;
    private FingerScanForm fingerScanForm;
    private RoomAssignmentForm roomAssignmentForm;
    private ScannersListPanel slp;

    private JButton btnSubmitForm;
    private JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private JButton btnAssignRoom, btnRemoveRoom;

    private final BuildingClient buildingClient = new BuildingClient();
    private final UserClient userClient = new UserClient();

    public AddUserTab() {
        super();

        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");
    }

    public ScannersListPanel getScannersListPanel() {
        return slp;
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        slp = new ScannersListPanel();
        slp.hideFingersCombo();
        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(slp, NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, CENTER);

        List<BuildingDTO> allBuildings = buildingClient.getAllBuildings();

        userInputForm = new UserInputForm();
        fingerScanForm = new FingerScanForm();
        roomAssignmentForm = new RoomAssignmentForm(allBuildings);

        btnScanThumb = fingerScanForm.getBtnScanThumb();
        btnScanThumb.addActionListener(this);
        btnScanIndex = fingerScanForm.getBtnScanIndex();
        btnScanIndex.addActionListener(this);
        btnScanMiddle = fingerScanForm.getBtnScanMiddle();
        btnScanMiddle.addActionListener(this);

        btnCancelThumbScan = fingerScanForm.getBtnCancelThumbScan();
        btnCancelThumbScan.addActionListener(this);
        btnCancelIndexScan = fingerScanForm.getBtnCancelIndexScan();
        btnCancelIndexScan.addActionListener(this);
        btnCancelMiddleScan = fingerScanForm.getBtnCancelMiddleScan();
        btnCancelMiddleScan.addActionListener(this);

        btnAssignRoom = roomAssignmentForm.getBtnAssignRoom();
        btnRemoveRoom = roomAssignmentForm.getBtnRemoveRoom();

        mainPanel.add(userInputForm, NORTH);
        mainPanel.add(fingerScanForm, CENTER);
        mainPanel.add(roomAssignmentForm, SOUTH);

        btnSubmitForm = new JButton("Submit Form");
        btnSubmitForm.setPreferredSize(new Dimension(btnSubmitForm.getPreferredSize().width, 20));
        btnSubmitForm.addActionListener(this);
        add(btnSubmitForm, SOUTH);
    }

    @Override
    protected void setDefaultValues() {
        scannedFingers.clear();
        currentFingerCapturing = FingerType.NONE;
        subject = null;

        userInputForm.clearFields();
    }

    @Override
    protected void updateControls() {
        boolean scanning = fingerScanForm.isScanning();

        btnScanThumb.setEnabled(!scanning);
        btnScanIndex.setEnabled(!scanning);
        btnScanMiddle.setEnabled(!scanning);

        btnCancelThumbScan.setEnabled(scanning);
        btnCancelIndexScan.setEnabled(scanning);
        btnCancelMiddleScan.setEnabled(scanning);

        btnAssignRoom.setEnabled(!scanning);
        btnRemoveRoom.setEnabled(!scanning);

//        btnSubmitForm.setEnabled(!scanning);
    }

    @Override
    public void updateFingersTools() {
        FingersTools.getInstance().getClient().reset();
        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        FingersTools.getInstance().getClient().setFingersReturnBinarizedImage(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(btnScanThumb)) {
            currentFingerCapturing = FingerType.THUMB;
            startCapturing(fingerScanForm.getThumbView());
        } else if (source.equals(btnScanIndex)) {
            currentFingerCapturing = FingerType.INDEX;
            startCapturing(fingerScanForm.getIndexView());
        } else if (source.equals(btnScanMiddle)) {
            currentFingerCapturing = FingerType.MIDDLE;
            startCapturing(fingerScanForm.getMiddleView());
        } else if (source.equals(btnCancelThumbScan) || source.equals(btnCancelIndexScan) || source.equals(btnCancelMiddleScan)) {
            cancelCapturing();
        } else if (source.equals(btnSubmitForm)) {
            saveUser();
        }
    }

    private void startCapturing(NFingerView view) {
        fingerScanForm.updateStatus("Starting capturing...");

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

        fingerScanForm.setScanning(true);
        updateControls();
    }

    private void cancelCapturing() {
        FingersTools.getInstance().getClient().cancel();

        fingerScanForm.setScanning(false);
        updateControls();
    }

    private void saveUser() {
        boolean valid = userInputForm.areAllFieldsValid();
        if (!valid) {
            showError("Please fill all fields!");
            return;
        }

        if (!fingerScanForm.areAllFingersScanned()) {
            showError("Not all fingers are scanned!");
            return;
        }

        String firstName = userInputForm.getFirstName();
        String lastName = userInputForm.getLastName();
        String pesel = userInputForm.getPesel();
        Role role = userInputForm.getRole();

        Map<BuildingDTO, List<RoomDTO>> selectedRooms = roomAssignmentForm.getSelectedRooms();

        Map<FingerType, byte[]> fingerprintData = scannedFingers.stream()
                .collect(Collectors.toMap(Fingerprint::fingerType, Fingerprint::token));

        List<Long> userRoomIds = selectedRooms.values()
                .stream()
                .flatMap(List::stream)
                .map(RoomDTO::roomId)
                .toList();

        UserCreationRequest request = new UserCreationRequest(firstName, lastName, pesel, role, fingerprintData, userRoomIds);
        System.out.println(request);
        userClient.createUser(request);
    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                fingerScanForm.setScanning(false);

                if (result.getStatus() == OK) {
                    fingerScanForm.updateStatus("Quality: " + subject.getFingers().get(0).getObjects().get(0).getQuality());

                    System.out.println("Saving finger: " + currentFingerCapturing);
                    scannedFingers.add(new Fingerprint(getFingerTemplate(subject), currentFingerCapturing));
                } else {
                    fingerScanForm.updateStatus("Failed to capture. " + result.getStatus());
                }

                currentFingerCapturing = FingerType.NONE;  // TODO: maybe to remove!
                updateControls();
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                fingerScanForm.setScanning(false);
                currentCapturingView.setShownImage(ORIGINAL);
                showError(throwable);
                currentFingerCapturing = FingerType.NONE;
                updateControls();
            });
        }
    }
}
