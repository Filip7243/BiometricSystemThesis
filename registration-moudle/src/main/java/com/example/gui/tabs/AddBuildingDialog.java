package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.CreateBuildingRequest;
import com.example.client.dto.CreateRoomRequest;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.Cursor.*;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class AddBuildingDialog extends JDialog {

    private final BuildingService buildingService;
    private final Consumer<BuildingDTO> refreshCallback;

    private DefaultListModel<CreateRoomRequest> roomListModel;

    public AddBuildingDialog(Frame parent,
                             BuildingService buildingService,
                             Consumer<BuildingDTO> refreshCallback) {
        super(parent, "Create new building", true);

        setLocationRelativeTo(parent);

        this.buildingService = buildingService;
        this.refreshCallback = refreshCallback;

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
        addRoomButton.addActionListener(e -> new AddOrUpdateRoomInBuildingDialog(
                (Frame) getParent(),
                false,
                null,
                roomListModel)
        );

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
                        refreshCallback.accept(result);
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
}
