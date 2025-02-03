package com.example.gui;

import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gui.StyledComponentFactory.createStyledButton;

public class RoomAssignmentForm extends JPanel {
    private final JList<BuildingDTO> buildingList;
    private final JList<RoomDTO> roomList;
    private final JTextArea selectionSummary;
    private final JButton btnAssignRoom;
    private final JButton btnRemoveRoom;

    private Map<BuildingDTO, List<RoomDTO>> buildingRoomMap;
    private final Map<BuildingDTO, List<RoomDTO>> selectedRooms;

    public RoomAssignmentForm(List<BuildingDTO> buildingsWithRooms) {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);

        selectedRooms = new HashMap<>();

        initializeData(buildingsWithRooms);

        // Styling building list
        buildingList = new JList<>(buildingRoomMap.keySet().toArray(new BuildingDTO[0]));
        buildingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingList.addListSelectionListener(e -> updateRoomList());
        buildingList.setCellRenderer(new BuildingRenderer());
        buildingList.setBackground(new Color(240, 240, 240));
        buildingList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane buildingScrollPane = new JScrollPane(buildingList);
        buildingScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Buildings",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        // Styling room list
        roomList = new JList<>();
        roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        roomList.setCellRenderer(new RoomRenderer());
        roomList.setBackground(new Color(240, 240, 240));
        roomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Rooms",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        // Styling selection summary
        selectionSummary = new JTextArea(5, 20);
        selectionSummary.setEditable(false);
        selectionSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectionSummary.setBackground(new Color(250, 250, 250));
        JScrollPane summaryScrollPane = new JScrollPane(selectionSummary);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Summary",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        // Styling buttons
        btnAssignRoom = createStyledButton("Assign Room", new Color(52, 152, 219), 150, 40);
        btnRemoveRoom = createStyledButton("Remove Room", new Color(231, 76, 60), 150, 40);

        btnAssignRoom.addActionListener(new AssignButtonListener());
        btnRemoveRoom.addActionListener(new RemoveButtonListener());

        // Layout configuration
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listsPanel.setBackground(Color.WHITE);
        listsPanel.add(buildingScrollPane);
        listsPanel.add(roomScrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAssignRoom);
        buttonPanel.add(btnRemoveRoom);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(summaryScrollPane, BorderLayout.CENTER);

        add(listsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Existing methods remain the same as in the original implementation
    JButton getBtnAssignRoom() {
        return btnAssignRoom;
    }

    JButton getBtnRemoveRoom() {
        return btnRemoveRoom;
    }

    Map<BuildingDTO, List<RoomDTO>> getSelectedRooms() {
        return selectedRooms;
    }

    void clearSelection() {
        selectedRooms.clear();
        updateSelectionSummary();
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

    // Inner classes remain the same
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
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            return c;
        }
    }

    private static class RoomRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value instanceof RoomDTO room) {
                value = room.roomNumber();
            }
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            return c;
        }
    }
}
