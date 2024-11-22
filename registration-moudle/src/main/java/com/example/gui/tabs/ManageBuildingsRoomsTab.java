package com.example.gui.tabs;

import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.gui.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;

public class ManageBuildingsRoomsTab extends BasePanel implements ActionListener {

    private final JFrame mainFrame;
    private final List<BuildingDTO> buildings = new ArrayList<>();
    private JTable buildingTable;
    private DefaultTableModel buildingTableModel;
    private JButton addBuildingButton;

    public ManageBuildingsRoomsTab(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        createBuildingTable();
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        add(scrollPane, CENTER);

        JButton addBuildingButton = new JButton("Add Building");
        addBuildingButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBuildingButton);

        add(buttonPanel, SOUTH);
    }

    @Override
    protected void setDefaultValues() {

    }

    @Override
    protected void updateControls() {

    }

    @Override
    protected void updateFingersTools() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showAddBuildingDialog();
    }

    private void createBuildingTable() {
        String[] cols = {"ID", "Building Number", "Street", "Edit", "Delete", "Details"};
        buildingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        buildingTable = new JTable(buildingTableModel);

        buildingTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        buildingTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        buildingTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());

        buildingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = buildingTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / buildingTable.getRowHeight();

                if (row < buildingTable.getRowCount() && row >= 0 && col < buildingTable.getColumnCount() && col >= 0) {
                    if (col == 3) {
                        editBuilding(row);
                    } else if (col == 4) {
                        deleteBuilding(row);
                    } else if (col == 5) {
                        showBuildingDetails(row);
                    }
                }
            }
        });
    }

    // TODO: change to mainFrame everywhere!
    private void showAddBuildingDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add Building", true);  // TODO: Get parent frame
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(20);
        JTextField streetField = new JTextField(20);
        DefaultListModel<RoomDTO> roomListModel = new DefaultListModel<>();
        JList<RoomDTO> roomList = new JList<>(roomListModel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Building Number:"), gbc);
        gbc.gridx = 1;
        panel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Street:"), gbc);
        gbc.gridx = 1;
        panel.add(streetField, gbc);

        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.addActionListener(e -> {  // TODO: here saving room to the db should be
            String roomNumber = JOptionPane.showInputDialog("Enter Room Number:");
            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                roomListModel.addElement(new RoomDTO((long) (roomListModel.size() + 1), roomNumber));
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addRoomButton, gbc);

        gbc.gridy = 3;
        panel.add(new JScrollPane(roomList), gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear");

        // todo; maybe here should be db saving
        saveButton.addActionListener(e -> {
            List<RoomDTO> rooms = new ArrayList<>();
            for (int i = 0; i < roomListModel.size(); i++) {
                rooms.add(roomListModel.get(i));
            }

            BuildingDTO building = new BuildingDTO(
                    (long) (buildings.size() + 1),
                    buildingNumberField.getText(),
                    streetField.getText(),
                    rooms
            );

            buildings.add(building);
            updateBuildingTable();
            dialog.dispose();
        });

        clearButton.addActionListener(e -> {
            buildingNumberField.setText("");
            streetField.setText("");
            roomListModel.clear();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 4;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void editBuilding(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog((JFrame) super.getParent().getParent(), "Edit Building", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(super.getParent().getParent());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(building.buildingNumber(), 20);
        JTextField streetField = new JTextField(building.street(), 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Building Number:"), gbc);
        gbc.gridx = 1;
        panel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Street:"), gbc);
        gbc.gridx = 1;
        panel.add(streetField, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            buildings.set(row, new BuildingDTO(
                    building.id(),
                    buildingNumberField.getText(),
                    streetField.getText(),
                    building.rooms()
            ));
            updateBuildingTable();
            dialog.dispose();
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteBuilding(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this building?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            buildings.remove(row);
            updateBuildingTable();
        }
    }

    private void showBuildingDetails(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog((JFrame) super.getParent().getParent(), "Building Details", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(super.getParent().getParent());

        JPanel panel = new JPanel(new BorderLayout());

        // Building info
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.add(new JLabel("Building Number:"));
        infoPanel.add(new JLabel(building.buildingNumber()));
        infoPanel.add(new JLabel("Street:"));
        infoPanel.add(new JLabel(building.street()));
        panel.add(infoPanel, BorderLayout.NORTH);

        // Rooms table
        String[] columnNames = {"Room ID", "Room Number", "Update", "Delete"};
        DefaultTableModel roomModel = new DefaultTableModel(columnNames, 0);
        JTable roomTable = new JTable(roomModel);

        // Add button columns
        roomTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        roomTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());

        // Populate rooms
        for (RoomDTO room : building.rooms()) {
            roomModel.addRow(new Object[]{
                    room.roomId(),
                    room.roomNumber(),
                    "Update",
                    "Delete"
            });
        }

        roomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = roomTable.getColumnModel().getColumnIndexAtX(evt.getX());
                int row = evt.getY() / roomTable.getRowHeight();

                if (row < roomTable.getRowCount() && row >= 0) {
                    if (column == 2) { // Update
                        updateRoom(building, row, dialog);
                    } else if (column == 3) { // Delete
                        deleteRoom(building, row, dialog);
                    }
                }
            }
        });

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void updateRoom(BuildingDTO building, int roomIndex, JDialog parentDialog) {
        RoomDTO room = building.rooms().get(roomIndex);
        String newRoomNumber = JOptionPane.showInputDialog(
                parentDialog,
                "Enter new room number:",
                room.roomNumber()
        );

        if (newRoomNumber != null && !newRoomNumber.trim().isEmpty()) {
            List<RoomDTO> updatedRooms = new ArrayList<>(building.rooms());
            updatedRooms.set(roomIndex, new RoomDTO(room.roomId(), newRoomNumber));

            int buildingIndex = buildings.indexOf(building);
            buildings.set(buildingIndex, new BuildingDTO(
                    building.id(),
                    building.buildingNumber(),
                    building.street(),
                    updatedRooms
            ));

            updateBuildingTable();
            parentDialog.dispose();
            showBuildingDetails(buildingIndex);
        }
    }

    private void deleteRoom(BuildingDTO building, int roomIndex, JDialog parentDialog) {
        int confirm = JOptionPane.showConfirmDialog(
                parentDialog,
                "Are you sure you want to delete this room?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            List<RoomDTO> updatedRooms = new ArrayList<>(building.rooms());
            updatedRooms.remove(roomIndex);

            int buildingIndex = buildings.indexOf(building);
            buildings.set(buildingIndex, new BuildingDTO(
                    building.id(),
                    building.buildingNumber(),
                    building.street(),
                    updatedRooms
            ));

            updateBuildingTable();
            parentDialog.dispose();
            showBuildingDetails(buildingIndex);
        }
    }

    private void updateBuildingTable() {
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
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
