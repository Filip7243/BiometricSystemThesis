package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.CreateBuildingRequest;
import com.example.client.dto.CreateRoomRequest;
import com.example.client.dto.RoomDTO;
import com.example.gui.ScannersListPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.BorderLayout.*;
import static java.awt.Cursor.*;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class AddBuildingDialog extends JDialog {

    private final List<BuildingDTO> buildings = new ArrayList<>();
    private final BuildingService buildingService;

    private DefaultTableModel buildingTableModel;
    private DefaultListModel<CreateRoomRequest> roomListModel;

    public AddBuildingDialog(Frame parent, BuildingService buildingService, DefaultTableModel buildingTableModel) {
        super(parent, "Create new building", true);

        setLocationRelativeTo(parent);

        this.buildingService = buildingService;
        this.buildingTableModel = buildingTableModel;

        initComponents();
    }

    private void initComponents() {
        setSize(600, 800);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(20);
        buildingNumberField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        JTextField streetField = new JTextField(20);
        streetField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        roomListModel = new DefaultListModel<>();
        JList<CreateRoomRequest> roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gbc.gridx = 0;
        gbc.gridy = 0;

        mainPanel.add(new JLabel("Building Number:"), gbc);

        gbc.gridx = 1;

        mainPanel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;

        mainPanel.add(new JLabel("Street:"), gbc);

        gbc.gridx = 1;

        mainPanel.add(streetField, gbc);

        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        addRoomButton.addActionListener(e -> {
            new AddRoomToBuildingForm();
        });

        JButton removeRoomButton = new JButton("Remove Room");
        removeRoomButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        removeRoomButton.addActionListener(e -> {
            int selectedIndex = roomList.getSelectedIndex();
            if (selectedIndex != -1) {
                roomListModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a room to remove.",
                        "No Selection",
                        WARNING_MESSAGE
                );
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;

        mainPanel.add(addRoomButton, gbc);

        gbc.gridy = 3;

        mainPanel.add(removeRoomButton, gbc);

        gbc.gridy = 4;

        mainPanel.add(new JScrollPane(roomList), gbc);

        JPanel buttonPanel = new JPanel();

        JButton saveButton = new JButton("Save");
        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));

        JButton clearButton = new JButton("Clear");
        clearButton.setCursor(getPredefinedCursor(HAND_CURSOR));

        saveButton.addActionListener(e -> {
            List<CreateRoomRequest> rooms = new ArrayList<>();
            for (int i = 0; i < roomListModel.size(); i++) {
                rooms.add(roomListModel.get(i));
            }

            CreateBuildingRequest request = new CreateBuildingRequest(
                    buildingNumberField.getText(),
                    streetField.getText(),
                    rooms
            );


            buildingService.saveBuilding(
                    request,
                    (result) -> {
                        BuildingDTO building = new BuildingDTO(
                                result.id(),
                                result.buildingNumber(),
                                result.street(),
                                result.rooms()
                        );
                        buildings.add(building);
                        updateBuildingTable();
                        dispose();
                    },
                    this
            );



        });

        clearButton.addActionListener(e -> {
            buildingNumberField.setText("");
            streetField.setText("");
            roomListModel.clear();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 5;

        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private void updateBuildingTable() {
        buildingService.getAllBuildings(
                buildings -> {
                    this.buildings.clear();
                    this.buildings.addAll(buildings);
                    buildingTableModel.setRowCount(0);
                    for (BuildingDTO building : buildings) {
                        buildingTableModel.addRow(new Object[]{
                                building.id(),
                                building.buildingNumber(),
                                building.street(),
                                "Edit",
                                "Delete",
                                "Details"
                        });
                    }
                },
                this
        );
    }

    private class AddRoomToBuildingForm extends JDialog {
        private JTextField txtFloor;
        private JTextField txtRoomNumber;

        public AddRoomToBuildingForm() {
            super(AddBuildingDialog.this, "Add Room to Building", true);

            setTitle("Fingerprint Registration");
            setSize(600, 500);
            setLayout(new BorderLayout(10, 10));

            JPanel inputPanel = createInputPanel();

            ScannersListPanel scannersPanel = new ScannersListPanel();
            scannersPanel.hideFingersCombo();

            JButton btnSubmit = new JButton("Submit Registration");
            btnSubmit.addActionListener(e -> submitRegistration());

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
            txtFloor = new JTextField(20);
            mainPanel.add(txtFloor, gbc);

            return mainPanel;
        }

        private void submitRegistration() {
            String roomNumber = txtRoomNumber.getText();
            String floor = txtFloor.getText();

            if (roomNumber.isBlank() || floor.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please fill in all fields.",
                        "Missing Fields",
                        WARNING_MESSAGE
                );
                return;
            }

            CreateRoomRequest request = new CreateRoomRequest(
                    roomNumber,
                    Integer.parseInt(floor),
                    buildings.get(buildings.size() - 1).id(),
                    null
            );

            RoomDTO room = new RoomDTO(
                    (long) (buildings.size() + 1),
                    roomNumber,
                    Integer.parseInt(floor),
                    null
            );

            roomListModel.addElement(room);

            dispose();
        }
    }
}
