package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CreateNewBuildingPanel extends JPanel implements ActionListener {
    private JTextField txtBuildingName;
    private JTextField txtRoomName;
    private DefaultListModel<String> roomListModel;
    private JList<String> roomList;
    private JButton btnAddRoom;
    private JButton btnCreateBuilding;

    public CreateNewBuildingPanel() {
        setBorder(BorderFactory.createTitledBorder("Create New Building and Add Rooms"));
        initGUI();
    }

    private void initGUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblBuildingName = new JLabel("Building Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblBuildingName, gbc);

        txtBuildingName = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtBuildingName, gbc);

        JLabel lblRoomName = new JLabel("Room Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblRoomName, gbc);

        txtRoomName = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtRoomName, gbc);

        btnAddRoom = new JButton("Add Room");
        btnAddRoom.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 1;
        add(btnAddRoom, gbc);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        JScrollPane roomListScrollPane = new JScrollPane(roomList);
        roomListScrollPane.setPreferredSize(new Dimension(150, 100));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(roomListScrollPane, gbc);

        btnCreateBuilding = new JButton("Create Building");
        btnCreateBuilding.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(btnCreateBuilding, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddRoom) {
            String roomName = txtRoomName.getText().trim();
            if (!roomName.isEmpty()) {
                roomListModel.addElement(roomName);
                txtRoomName.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Room name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnCreateBuilding) {
            String buildingName = txtBuildingName.getText().trim();
            if (buildingName.isEmpty() || roomListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Building name and rooms cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                List<String> rooms = new ArrayList<>();
                for (int i = 0; i < roomListModel.size(); i++) {
                    rooms.add(roomListModel.getElementAt(i));
                }
                JOptionPane.showMessageDialog(this, "Building \"" + buildingName + "\" with rooms " + rooms + " created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtBuildingName.setText("");
                roomListModel.clear();
            }
        }
    }
}
