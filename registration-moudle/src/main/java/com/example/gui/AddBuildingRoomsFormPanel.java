package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class AddBuildingRoomsFormPanel extends JPanel implements ActionListener {
    private JTextField buildingNameField;
    private JTextField roomNameField;
    private JButton addBuildingButton;
    private JButton addRoomButton;
    private JButton assignRoomsButton;
    private JComboBox<String> buildingDropdown;
    private DefaultComboBoxModel<String> buildingModel;
    private List<String> roomsList;
    private List<Building> buildings;

    public AddBuildingRoomsFormPanel() {
        // Initialize fields and components
        buildingNameField = new JTextField(15);
        roomNameField = new JTextField(15);
        addBuildingButton = new JButton("Add Building");
        addRoomButton = new JButton("Add Room");
        assignRoomsButton = new JButton("Assign Rooms to Building");
        buildingModel = new DefaultComboBoxModel<>();
        buildingDropdown = new JComboBox<>(buildingModel);
        roomsList = new ArrayList<>();
        buildings = new ArrayList<>();

        // Set layout
        setLayout(new GridLayout(0, 2, 10, 10));

        // Add components to panel
        add(new JLabel("Building Name:"));
        add(buildingNameField);
        add(addBuildingButton);

        add(new JLabel("Select Building:"));
        add(buildingDropdown);

        add(new JLabel("Room Name:"));
        add(roomNameField);
        add(addRoomButton);

        add(assignRoomsButton);

        // Add action listeners
        addBuildingButton.addActionListener(this);
        addRoomButton.addActionListener(this);
        assignRoomsButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBuildingButton) {
            String buildingName = buildingNameField.getText().trim();
            if (buildingName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Building name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else {
                buildings.add(new Building(buildingName));
                buildingModel.addElement(buildingName);
                buildingNameField.setText("");
                JOptionPane.showMessageDialog(this, "Building added successfully.");
            }
        } else if (e.getSource() == addRoomButton) {
            String roomName = roomNameField.getText().trim();
            if (roomName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else {
                roomsList.add(roomName);
                roomNameField.setText("");
                JOptionPane.showMessageDialog(this, "Room added to the list.");
            }
        } else if (e.getSource() == assignRoomsButton) {
            String selectedBuilding = (String) buildingDropdown.getSelectedItem();
            if (selectedBuilding == null || selectedBuilding.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No building selected.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else if (roomsList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No rooms to assign.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } else {
                for (Building building : buildings) {
                    if (building.getName().equals(selectedBuilding)) {
                        building.addRooms(roomsList);
                        roomsList.clear();
                        JOptionPane.showMessageDialog(this, "Rooms assigned successfully to " + selectedBuilding);
                        break;
                    }
                }
            }
        }
    }

    private static class Building {
        private String name;
        private List<String> rooms;

        public Building(String name) {
            this.name = name;
            this.rooms = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void addRooms(List<String> roomsToAdd) {
            this.rooms.addAll(roomsToAdd);
        }

        @Override
        public String toString() {
            return name + " (" + rooms.size() + " rooms)";
        }
    }
}
