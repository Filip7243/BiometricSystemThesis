package com.example.gui;

import com.example.FingersTools;
import com.example.client.BuildingClient;
import com.example.client.UserClient;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UserCreationRequest;
import com.example.model.FingerType;
import com.example.model.Fingerprint;
import com.example.model.Role;
import com.example.utils.EncryptionUtils;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.neurotec.biometrics.NBiometricOperation.CAPTURE;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class AddUserTab extends BasePanel implements ActionListener {

    private final BuildingClient buildingClient = new BuildingClient();
    private final UserClient userClient = new UserClient();
    private final CaptureHandler captureHandler = new CaptureHandler();
    private final List<Fingerprint> scannedFingers = new ArrayList<>();

    private FingerType currentFingerCapturing = FingerType.NONE;

    private NSubject subject;

    private UserInputForm userInputForm;
    private FingerScanForm fingerScanForm;
    private RoomAssignmentForm roomAssignmentForm;
    private ScannersListPanel slp;

    private JButton btnSubmitForm;
    private JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private JButton btnAssignRoom, btnRemoveRoom;

    private JTabbedPane tabbedPane;
    private JPanel mainPanel;

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

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Create New User", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray
        headerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(headerTitle);

        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);

        slp = new ScannersListPanel();
        slp.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(slp, BorderLayout.CENTER);

        add(headerPanel, NORTH);

        // Create a main container for flexible layout
        mainPanel = new JPanel();

        // Create a tabbed pane for mobile view
        tabbedPane = new JTabbedPane();

        List<BuildingDTO> allBuildings = buildingClient.getAllBuildings("");

        userInputForm = new UserInputForm();
        fingerScanForm = new FingerScanForm();
        roomAssignmentForm = new RoomAssignmentForm(allBuildings);

        styleButtons(fingerScanForm, roomAssignmentForm);

        // Determine screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        boolean isSmallScreen = screenSize.height < 900; // Adjust this threshold as needed

        if (isSmallScreen) {
            // For small screens, use tabbed pane
            mainPanel.setLayout(new BorderLayout());

            JScrollPane userInputScrollPane = createScrollPane(userInputForm);
            JScrollPane fingerScanScrollPane = createScrollPane(fingerScanForm);
            JScrollPane roomAssignmentScrollPane = createScrollPane(roomAssignmentForm);

            tabbedPane.addTab("User Info", userInputScrollPane);
            tabbedPane.addTab("Finger Scan", fingerScanScrollPane);
            tabbedPane.addTab("Room Assignment", roomAssignmentScrollPane);

            mainPanel.add(tabbedPane, CENTER);
        } else {
            // For larger screens, use traditional vertical layout
            mainPanel.setLayout(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            mainPanel.add(userInputForm, NORTH);
            mainPanel.add(fingerScanForm, CENTER);
            mainPanel.add(roomAssignmentForm, SOUTH);
        }

        add(mainPanel, CENTER);

        btnSubmitForm = createStyledButton("Submit", new Color(46, 204, 113), 150, 40);
        btnSubmitForm.addActionListener(this);

        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        submitPanel.add(btnSubmitForm);

        add(submitPanel, BorderLayout.SOUTH);
    }

    // Helper method to create scrollable panes with consistent styling
    private JScrollPane createScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }


    @Override
    protected void setDefaultValues() {
        scannedFingers.clear();
        currentFingerCapturing = FingerType.NONE;
        subject = null;

        userInputForm.clearFields();
        roomAssignmentForm.clearSelection();
        fingerScanForm.clearViews();
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

    private void styleButtons(FingerScanForm fingerScanForm, RoomAssignmentForm roomAssignmentForm) {
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
                .createTask(EnumSet.of(CAPTURE), subject);
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

        if (roomAssignmentForm.getSelectedRooms().isEmpty()) {
            showError("Please assign a room to the user!");
            return;
        }

        String firstName = userInputForm.getFirstName();
        String lastName = userInputForm.getLastName();
        String pesel = userInputForm.getPesel();
        Role role = userInputForm.getRole();

        Map<BuildingDTO, List<RoomDTO>> selectedRooms = roomAssignmentForm.getSelectedRooms();

        Map<FingerType, byte[]> encryptedFingerprintData = new HashMap<>();

        for (Fingerprint fingerprint : scannedFingers) {
            try {
                System.out.println("Encrypting image data for " + fingerprint.fingerType());
                byte[] encryptedImage = EncryptionUtils.encrypt(fingerprint.originalImage());
                encryptedFingerprintData.put(fingerprint.fingerType(), encryptedImage);
            } catch (Exception e) {
//                logger.error("Failed to encrypt fingerprint data", e);
                showError("Error processing fingerprint data!");
                return;
            }
        }

        List<Long> userRoomIds = selectedRooms.values()
                .stream()
                .flatMap(List::stream)
                .map(RoomDTO::roomId)
                .toList();

        UserCreationRequest request = new UserCreationRequest(
                firstName,
                lastName,
                pesel,
                role,
                encryptedFingerprintData,  // Now using encrypted data
                userRoomIds
        );

        userClient.createUser(request);

        setDefaultValues();

        showMessageDialog(this, "User created successfully!", "Success", PLAIN_MESSAGE);
    }

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                fingerScanForm.setScanning(false);

                if (result.getStatus() == OK) {
                    System.out.println("Saving finger: " + currentFingerCapturing);
                    Optional<Fingerprint> duplicate = scannedFingers.stream()
                            .filter(f -> f.fingerType().equals(currentFingerCapturing))
                            .findFirst();

                    if (duplicate.isPresent()) {
                        scannedFingers.set(scannedFingers.indexOf(duplicate.get()), new Fingerprint(
                                currentFingerCapturing,
                                subject.getFingers().get(0).getImage().save().toByteArray()
                        ));
                    } else {
                        scannedFingers.add(new Fingerprint(
                                currentFingerCapturing,
                                subject.getFingers().get(0).getImage().save().toByteArray()
                        ));
                    }
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
                showError(throwable);
                currentFingerCapturing = FingerType.NONE;
                updateControls();
            });
        }
    }
}
