package com.example.gui.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAssignmentForm extends JPanel {
    private JList<String> buildingList;
    private JList<String> roomList;
    private JTextArea selectionSummary;
    private JButton btnAssignRoom, btnRemoveRoom;

    private Map<String, List<String>> buildingRoomMap, selectedRooms;

    public RoomAssignmentForm() {
        setLayout(new BorderLayout());

        initializeData();

        selectedRooms = new HashMap<>();

        buildingList = new JList<>(buildingRoomMap.keySet().toArray(new String[0]));
        buildingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingList.addListSelectionListener(e -> updateRoomList());
        JScrollPane buildingScrollPane = new JScrollPane(buildingList);
        buildingScrollPane.setBorder(BorderFactory.createTitledBorder("Buildings"));

        roomList = new JList<>();
        roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setBorder(BorderFactory.createTitledBorder("Rooms"));

        selectionSummary = new JTextArea(5, 20);
        selectionSummary.setEditable(false);
        JScrollPane summaryScrollPane = new JScrollPane(selectionSummary);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("Selection Summary"));

        btnAssignRoom = new JButton("Assign Rooms");
        btnAssignRoom.addActionListener(new AssignButtonListener());

        btnRemoveRoom = new JButton("Remove Selected Room");
        btnRemoveRoom.addActionListener(new RemoveButtonListener());

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listsPanel.add(buildingScrollPane);
        listsPanel.add(roomScrollPane);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(btnAssignRoom, BorderLayout.NORTH);
        bottomPanel.add(summaryScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnAssignRoom);
        buttonPanel.add(btnRemoveRoom);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(listsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    JButton getBtnAssignRoom() {
        return btnAssignRoom;
    }

    JButton getBtnRemoveRoom() {
        return btnRemoveRoom;
    }

    Map<String, List<String>> getBuildingRoomMap() {
        return buildingRoomMap;
    }

    Map<String, List<String>> getSelectedRooms() {
        return selectedRooms;
    }

    private void initializeData() {
        buildingRoomMap = new HashMap<>();
        buildingRoomMap.put("Building A", List.of("Room 101", "Room 102", "Room 103"));
        buildingRoomMap.put("Building B", List.of("Room 201", "Room 202", "Room 203", "Room 204"));
        buildingRoomMap.put("Building C", List.of("Room 301", "Room 302"));
    }

    private void updateRoomList() {
        String selectedBuilding = buildingList.getSelectedValue();
        if (selectedBuilding != null) {
            List<String> rooms = buildingRoomMap.get(selectedBuilding);
            roomList.setListData(rooms.toArray(new String[0]));
        }
    }

    private class AssignButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedBuilding = buildingList.getSelectedValue();
            List<String> selectedRoomList = roomList.getSelectedValuesList();

            if (selectedBuilding == null || selectedRoomList.isEmpty()) {
                JOptionPane.showMessageDialog(RoomAssignmentForm.this,
                        "Please select a building and at least one room.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            selectedRooms.put(selectedBuilding, new ArrayList<>(selectedRoomList));

            updateSelectionSummary();
        }
    }

    private class RemoveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedBuilding = buildingList.getSelectedValue();
            List<String> selectedRoom = roomList.getSelectedValuesList();

            if (selectedBuilding == null || selectedRoom == null || selectedRoom.isEmpty()) {
                JOptionPane.showMessageDialog(RoomAssignmentForm.this,
                        "Please select a room from the selection summary to remove.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> roomsInBuilding = selectedRooms.get(selectedBuilding);
            if (roomsInBuilding != null) {
                roomsInBuilding.removeAll(selectedRoom);
                if (roomsInBuilding.isEmpty()) {
                    selectedRooms.remove(selectedBuilding);
                }
            }

            updateSelectionSummary();
        }
    }

    private void updateSelectionSummary() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : selectedRooms.entrySet()) {
            summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        selectionSummary.setText(summary.toString());
    }
}
