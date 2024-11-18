package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpdateRoomsInBuildingPanel extends JPanel implements ActionListener {
    private JList<String> buildingList;
    private JList<String> roomList;
    private JButton btnAddRoom;
    private JButton btnRemoveRoom;
    private JTextField txtNewRoom;

    public UpdateRoomsInBuildingPanel() {
        setBorder(BorderFactory.createTitledBorder("Update Rooms in Building"));
        initGUI();
    }

    private void initGUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        buildingList = new JList<>(new String[]{"Building 1", "Building 2", "Building 3"});
        JScrollPane buildingScrollPane = new JScrollPane(buildingList);
        buildingScrollPane.setPreferredSize(new Dimension(150, 100));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        add(buildingScrollPane, gbc);

        roomList = new JList<>(new String[]{"Room 101", "Room 102", "Room 201"});
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setPreferredSize(new Dimension(150, 100));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        add(roomScrollPane, gbc);

        JLabel lblNewRoom = new JLabel("New Room:");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        add(lblNewRoom, gbc);

        txtNewRoom = new JTextField(10);
        gbc.gridx = 2;
        gbc.gridy = 1;
        add(txtNewRoom, gbc);

        btnAddRoom = new JButton("Add Room");
        btnAddRoom.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(btnAddRoom, gbc);

        btnRemoveRoom = new JButton("Remove Room");
        btnRemoveRoom.addActionListener(this);
        gbc.gridx = 2;
        gbc.gridy = 3;
        add(btnRemoveRoom, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddRoom) {
            String newRoom = txtNewRoom.getText().trim();
            if (!newRoom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room \"" + newRoom + "\" added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtNewRoom.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Room name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnRemoveRoom) {
            String selectedRoom = roomList.getSelectedValue();
            if (selectedRoom != null) {
                JOptionPane.showMessageDialog(this, "Room \"" + selectedRoom + "\" removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No room selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
