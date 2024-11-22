package com.example.gui.tabs;

import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.gui.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Cursor.*;
import static java.awt.Font.BOLD;
import static javax.swing.SwingConstants.CENTER;

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

        add(new JLabel("Buildings and Rooms Managing!"), NORTH);

        createBuildingTable();
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        add(scrollPane, BorderLayout.CENTER);

        addBuildingButton = new JButton("Add Building");
        addBuildingButton.addActionListener(this);
        addBuildingButton.setCursor(getPredefinedCursor(HAND_CURSOR));

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
        if (e.getSource().equals(addBuildingButton)) {
            showAddBuildingDialog();
        }
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
        buildingTable.setShowGrid(true);
        buildingTable.setGridColor(Color.LIGHT_GRAY);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(CENTER);
        buildingTable.getColumnModel().getColumn(0).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(1).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(2).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Edit"));
        buildingTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Delete"));
        buildingTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Details"));

        buildingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = buildingTable.rowAtPoint(e.getPoint());
                int col = buildingTable.columnAtPoint(e.getPoint());

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

        buildingTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                buildingTable.setCursor(getPredefinedCursor(HAND_CURSOR));
            }
        });
    }

    private void showAddBuildingDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add Building", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(20);
        buildingNumberField.setCursor(getPredefinedCursor(TEXT_CURSOR));
        JTextField streetField = new JTextField(20);
        streetField.setCursor(getPredefinedCursor(TEXT_CURSOR));
        DefaultListModel<RoomDTO> roomListModel = new DefaultListModel<>();
        JList<RoomDTO> roomList = new JList<>(roomListModel);  //TODO: dodac usuwanie pokoju przy dodawaniu jakbym sie pomylil
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        addRoomButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        addRoomButton.addActionListener(e -> {  // TODO: here saving room to the db should be
            String roomNumber = JOptionPane.showInputDialog("Enter Room Number:");
            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                roomListModel.addElement(new RoomDTO((long) (roomListModel.size() + 1), roomNumber));
            }
        });

        JButton removeRoomButton = new JButton("Remove Room");
        removeRoomButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeRoomButton.addActionListener(e -> {
            int selectedIndex = roomList.getSelectedIndex();
            if (selectedIndex != -1) {
                roomListModel.remove(selectedIndex); // Remove the selected room
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a room to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addRoomButton, gbc);

        gbc.gridy = 3;
        panel.add(removeRoomButton, gbc);

        gbc.gridy = 4;
        panel.add(new JScrollPane(roomList), gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        JButton clearButton = new JButton("Clear");
        clearButton.setCursor(getPredefinedCursor(HAND_CURSOR));

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

        gbc.gridy = 5;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void editBuilding(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog(mainFrame, "Edit Building", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(building.buildingNumber(), 20);
        buildingNumberField.setCursor(getPredefinedCursor(TEXT_CURSOR));
        JTextField streetField = new JTextField(building.street(), 20);
        streetField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblBuildingNumber = new JLabel("Building Number:");
        lblBuildingNumber.setFont(new Font("Arial", BOLD, 12));
        panel.add(lblBuildingNumber, gbc);
        gbc.gridx = 1;
        panel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblStreet = new JLabel("Street:");
        lblStreet.setFont(new Font("Arial", BOLD, 12));
        panel.add(lblStreet, gbc);
        gbc.gridx = 1;
        panel.add(streetField, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));
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
                "Are you sure you want to delete this building? The rooms will be deleted too!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            buildings.remove(row);  // TODO: here remove buildings from db
            updateBuildingTable();
        }
    }

    private void showBuildingDetails(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog(mainFrame, "Building Details", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel lblBuildingNumber = new JLabel("Building Number:");
        lblBuildingNumber.setFont(new Font("Arial", BOLD, 12));
        infoPanel.add(lblBuildingNumber);
        infoPanel.add(new JLabel(building.buildingNumber()));
        JLabel lblStreet = new JLabel("Street:");
        lblStreet.setFont(new Font("Arial", BOLD, 12));  // TODO: maybe in futre create my own Label ex. BoldLabel etc.
        infoPanel.add(lblStreet);
        infoPanel.add(new JLabel(building.street()));
        panel.add(infoPanel, NORTH);

        String[] columnNames = {"Room ID", "Room Number", "Update", "Delete"};
        DefaultTableModel roomModel = new DefaultTableModel(columnNames, 0);
        JTable roomTable = new JTable(roomModel);
        roomTable.setShowGrid(true);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(CENTER);
        roomTable.getColumnModel().getColumn(0).setCellRenderer(defaultTableCellRenderer);
        roomTable.getColumnModel().getColumn(1).setCellRenderer(defaultTableCellRenderer);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer("Edit"));
        roomTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Delete"));

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
            public void mouseClicked(MouseEvent e) {
                int row = buildingTable.rowAtPoint(e.getPoint());
                int col = buildingTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0) {
                    if (col == 2) { // Update
                        updateRoom(building, row, dialog);
                    } else if (col == 3) { // Delete
                        deleteRoom(building, row, dialog);
                    }
                }
            }
        });

        roomTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                roomTable.setCursor(getPredefinedCursor(HAND_CURSOR));
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

        if (confirm == JOptionPane.YES_OPTION) {  // tODO: delete building rom db here
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
        public ButtonRenderer(String function) {
            setOpaque(true);

            switch (function) {
                case "Edit":
                    setBackground(Color.LIGHT_GRAY);
                    setFont(new Font("Arial", BOLD, 12));
                    break;
                case "Delete":
                    setBackground(new Color(255, 51, 0));
                    setFont(new Font("Arial", BOLD, 12));
                    break;
                case "Details":
                    setBackground(Color.LIGHT_GRAY);
                    setFont(new Font("Arial", BOLD, 12));
                    break;
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
