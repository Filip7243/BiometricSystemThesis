package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.gui.BasePanel;
import com.example.gui.LicensingPanel;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFingerView;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;

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

    private LicensingPanel panelLicensing;
    private UserInputForm userInputForm;
    private FingerScanForm fingerScanForm;
    private RoomAssignmentForm roomAssignmentForm;

    private JButton btnSubmitForm;
    private JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private JButton btnAssignRoom, btnRemoveRoom;

    public AddUserTab() {
        super();

        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Biometrics.FingerExtraction");
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
        optionalLicenses.add("Images.WSQ");
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(panelLicensing, NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, CENTER);

        userInputForm = new UserInputForm();
        fingerScanForm = new FingerScanForm();
        roomAssignmentForm = new RoomAssignmentForm();

        btnScanThumb = fingerScanForm.getBtnScanThumb();
        btnScanIndex = fingerScanForm.getBtnScanIndex();
        btnScanMiddle = fingerScanForm.getBtnScanMiddle();

        btnCancelThumbScan = fingerScanForm.getBtnCancelThumbScan();
        btnCancelIndexScan = fingerScanForm.getBtnCancelIndexScan();
        btnCancelMiddleScan = fingerScanForm.getBtnCancelMiddleScan();

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
        //
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

        btnSubmitForm.setEnabled(!scanning);
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
        if (source.equals(btnScanThumb)) {
            System.out.println("THUMB");
            startCapturing(fingerScanForm.getThumbView());
        } else if (source.equals(btnScanIndex)) {
            System.out.println("INDEX");
            startCapturing(fingerScanForm.getIndexView());
        } else if (source.equals(btnScanMiddle)) {
            System.out.println("MIDDLE");
            startCapturing(fingerScanForm.getMiddleView());
        }
    }

    private void startCapturing(NFingerView view) {
        fingerScanForm.updateStatus("Starting capturing...");  // TODO: add eventlistneres to btns

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

    private final class CaptureHandler implements CompletionHandler<NBiometricTask, Object> {
        @Override
        public void completed(final NBiometricTask result, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                fingerScanForm.setScanning(false);

//                fingerScanForm.updateShownImage();
                if (result.getStatus() == OK) {
                    fingerScanForm.updateStatus("Quality: " + subject.getFingers().get(0).getObjects().get(0).getQuality());
//                    if (scannedFingers.size() < 3) {
//                        scannedFingers.add(subject);
//                    }
                } else {
                    fingerScanForm.updateStatus("Failed to capture. " + result.getStatus());
                }

                updateControls();
            });
        }

        @Override
        public void failed(final Throwable throwable, final Object attachment) {
            SwingUtilities.invokeLater(() -> {
                fingerScanForm.setScanning(false);
//                fingerScanForm.updateShownImage();
                showError(throwable);
                updateControls();
            });
        }
    }
}
