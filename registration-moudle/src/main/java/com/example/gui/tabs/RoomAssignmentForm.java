package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.model.Building;
import com.example.model.Room;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NFingerScanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAssignmentForm extends JPanel {
    // TODO: change to models
    private JList<BuildingDTO> buildingList;
    private JList<RoomDTO> roomList;
    private JTextArea selectionSummary;
    private JButton btnAssignRoom, btnRemoveRoom;

    private Map<BuildingDTO, List<RoomDTO>> buildingRoomMap, selectedRooms;

    public RoomAssignmentForm(List<BuildingDTO> buildingsWithRooms) {
        setLayout(new BorderLayout());

        selectedRooms = new HashMap<>();

        initializeData(buildingsWithRooms);

        buildingList = new JList<>(buildingRoomMap.keySet().toArray(new BuildingDTO[0]));
//        buildingList = new JList<>();
        buildingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingList.addListSelectionListener(e -> updateRoomList());
        buildingList.setCellRenderer(new BuildingRenderer());
        JScrollPane buildingScrollPane = new JScrollPane(buildingList);
        buildingScrollPane.setBorder(BorderFactory.createTitledBorder("Buildings"));

        roomList = new JList<>();
        roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        roomList.setCellRenderer(new RoomRenderer());
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

    Map<BuildingDTO, List<RoomDTO>> getBuildingRoomMap() {
        return buildingRoomMap;
    }

    Map<BuildingDTO, List<RoomDTO>> getSelectedRooms() {
        return selectedRooms;
    }

    private void initializeData(List<BuildingDTO> buildingsWithRooms) {
        buildingRoomMap = new HashMap<>();

        buildingsWithRooms.forEach(building -> buildingRoomMap.put(building, building.rooms()));
    }

    private void updateRoomList() {
        BuildingDTO selectedBuilding = buildingList.getSelectedValue();
        if (selectedBuilding != null) {
            List<RoomDTO> rooms = buildingRoomMap.get(selectedBuilding);
            roomList.setListData(rooms.toArray(new RoomDTO[0]));
        }
    }

    private void updateSelectionSummary() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<BuildingDTO, List<RoomDTO>> entry : selectedRooms.entrySet()) {
            summary.append(entry.getKey().buildingNumber()).append(": ");
            for (RoomDTO room : entry.getValue()) {
                summary.append(room.roomNumber()).append(", ");
            }
            summary.setLength(summary.length() - 2); // Remove last comma
            summary.append("\n");
        }
        selectionSummary.setText(summary.toString());
    }


    private class AssignButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            BuildingDTO selectedBuilding = buildingList.getSelectedValue();
            List<RoomDTO> selectedRoomList = roomList.getSelectedValuesList();

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
            BuildingDTO selectedBuilding = buildingList.getSelectedValue();
            List<RoomDTO> selectedRoomList = roomList.getSelectedValuesList();

            if (selectedBuilding == null || selectedRoomList.isEmpty()) {
                JOptionPane.showMessageDialog(RoomAssignmentForm.this,
                        "Please select a room to remove.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<RoomDTO> roomsInBuilding = selectedRooms.get(selectedBuilding);
            if (roomsInBuilding != null) {
                roomsInBuilding.removeAll(selectedRoomList);
                if (roomsInBuilding.isEmpty()) {
                    selectedRooms.remove(selectedBuilding);
                }
            }

            updateSelectionSummary();
        }
    }

    private static class BuildingRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof BuildingDTO building) {
                value = building.buildingNumber();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class RoomRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof RoomDTO room) {
                value = room.roomNumber();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
