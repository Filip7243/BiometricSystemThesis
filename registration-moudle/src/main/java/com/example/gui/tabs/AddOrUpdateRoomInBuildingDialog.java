package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.dto.AddRoomRequest;
import com.example.client.dto.CreateRoomRequest;
import com.example.gui.ScannersListPanel;
import com.neurotec.devices.NFScanner;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class AddOrUpdateRoomInBuildingDialog extends JDialog {
    private final DefaultListModel<CreateRoomRequest> roomListModel;
    private final Consumer<AddRoomRequest> updateBuildingCallback;

    private JTextField txtRoomNumber;
    private JSpinner floorSpinner;

    public AddOrUpdateRoomInBuildingDialog(Frame parent,
                                           boolean isUpdate,
                                           Consumer<AddRoomRequest> updateBuildingCallback,
                                           DefaultListModel<CreateRoomRequest> roomListModel) {
        super(parent, "Add Room to Building", true);


        setTitle("Fingerprint Registration");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = createInputPanel();

        ScannersListPanel scannersPanel = new ScannersListPanel();
        scannersPanel.hideFingersCombo();

        this.updateBuildingCallback = updateBuildingCallback;

        this.roomListModel = roomListModel;

        JButton btnSubmit = new JButton("Submit Registration");
        btnSubmit.addActionListener(e -> {
            if (isUpdate) {
                updateBuilding();
            } else {
                addRoomToForm();
            }
        });

        add(inputPanel, NORTH);
        add(scannersPanel, CENTER);
        add(btnSubmit, SOUTH);

        scannersPanel.updateScannerList();

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Registration Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Room Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtRoomNumber = new JTextField(20);
        mainPanel.add(txtRoomNumber, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Floor:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        mainPanel.add(floorSpinner, gbc);

        return mainPanel;
    }

    private void addRoomToForm() {
        String roomNumber = txtRoomNumber.getText();
        int floor = (int) floorSpinner.getValue();

        if (roomNumber.isBlank() || floor < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields.",
                    "Missing Fields",
                    WARNING_MESSAGE
            );
            return;
        }

        CreateRoomRequest room = new CreateRoomRequest(
                roomNumber,
                floor,
                FingersTools.getInstance().getClient().getFingerScanner() != null ?
                        FingersTools.getInstance().getClient().getFingerScanner().getId() : null
        );

        System.out.println(room);

        roomListModel.addElement(room);

        dispose();
    }

    private void updateBuilding() {
        String roomNumber = txtRoomNumber.getText().trim();
        int floor = (int) floorSpinner.getValue();

        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Room number cannot be empty",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        NFScanner selectedScanner = FingersTools
                .getInstance()
                .getClient()
                .getFingerScanner();

        String hardwareDeviceId = selectedScanner != null ? selectedScanner.getId() : null;

        if (updateBuildingCallback != null) {
            updateBuildingCallback.accept(new AddRoomRequest(roomNumber, floor, 1L, hardwareDeviceId));

            dispose();
        }
    }
}
