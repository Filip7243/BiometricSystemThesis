package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomClient;
import com.example.client.RoomService;
import com.example.client.dto.AddRoomRequest;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.gui.ScannersListPanel;
import com.example.gui.tabs.tables.MyTable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import static java.awt.BorderLayout.*;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.FlowLayout.RIGHT;
import static java.awt.Font.BOLD;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.SwingConstants.CENTER;

public class BuildingDetailsDialog extends JDialog {

    private final RoomClient roomClient = new RoomClient();
    private final RoomService roomService = new RoomService(roomClient);
    private final BuildingService buildingService;
    private final Runnable onBuildingUpdated;

    private BuildingDTO building;
    private DefaultTableModel roomTableModel;

    public BuildingDetailsDialog(Frame owner,
                                 BuildingDTO building,
                                 BuildingService buildingService,
                                 Runnable onBuildingUpdated) {
        super(owner, true);

        this.building = building;
        this.buildingService = buildingService;
        this.onBuildingUpdated = onBuildingUpdated;

        initComponent();
    }

    private void initComponent() {
        setTitle("Details for building " + building.buildingNumber());
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Building Details", 0);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray

        JLabel headerDetails = new JLabel(
                "Building Number: " + building.buildingNumber() + " | Street: " + building.street(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray

        // Button Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JButton addRoomButton = createStyledButton("Add Room");
        buttonPanel.add(addRoomButton, CENTER);

        headerPanel.add(headerTitle, NORTH);
        headerPanel.add(headerDetails, CENTER);
        headerPanel.add(buttonPanel, SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Room Table
        String[] columnNames = {"Room ID", "Room Number", "Floor", "Update", "Delete", "Set Device"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        MyTable roomTable = new MyTable(roomTableModel);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        roomTable.setColumnRenderer(0, defaultTableCellRenderer);
        roomTable.setColumnRenderer(1, defaultTableCellRenderer);
        roomTable.setColumnRenderer(2, defaultTableCellRenderer);
        roomTable.setColumnRenderer(3, new ButtonRenderer("Edit"));
        roomTable.setColumnRenderer(4, new ButtonRenderer("Delete"));
        roomTable.setColumnRenderer(5, new ButtonRenderer("Set Device"));

        // Add rows to the table
        for (RoomDTO room : building.rooms()) {
            roomTableModel.addRow(new Object[]{
                    room.roomId(),
                    room.roomNumber(),
                    room.floor(),
                    "Update",
                    "Delete",
                    "Set Device"
            });
        }

        addRoomButton.addActionListener(e -> {
            new AddOrUpdateRoomInBuildingDialog(
                    (Frame) getParent(),
                    true,
                    (request) -> buildingService.addRoomToBuilding(
                            new AddRoomRequest(request.roomNumber(), request.floor(), request.macAddress(), request.scannerSerialNumber(), building.id()),
                            (room) -> {
                                onBuildingUpdated.run();
                                updateRoomTable();
                                updateBuilding();
                            },
                            this),
                    null,
                    "Add New Room",
                    building
            );
        });

        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = roomTable.rowAtPoint(e.getPoint());
                int col = roomTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0) {
                    if (col == 3) { // Update
                        updateRoom(row);
                    } else if (col == 4) { // Delete
                        deleteRoom(row);
                    } else if (col == 5) { // Set Device
                        assignDevice(row);
                    }
                }
            }
        });

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        add(panel);
        setVisible(true);
    }

    private void updateRoomTable() {
        buildingService.getBuildingById(building.id(), (building) -> {
            roomTableModel.setRowCount(0);
            for (RoomDTO room : building.rooms()) {
                System.out.println("Room: " + room.roomId());
                roomTableModel.addRow(new Object[]{
                        room.roomId(),
                        room.roomNumber(),
                        room.floor(),
                        "Update",
                        "Delete",
                        "Set Device"
                });
            }
        }, this);
    }

    private void assignDevice(int row) {
        Long roomId = (Long) roomTableModel.getValueAt(row, 0);
        roomService.getRoomById(
                roomId,
                (room) -> new AssignDeviceToRoomDialog(
                        (Frame) SwingUtilities.getWindowAncestor(getParent()),
                        roomService,
                        room,
                        buildingService,
                        building,
                        () -> {
                            updateRoomTable();
                            if (onBuildingUpdated != null) {
                                onBuildingUpdated.run();
                            }
                            updateBuilding();
                        }
                ),
                null
        );
    }

    private void updateBuilding() {
        buildingService.getBuildingById(
                building.id(),
                (building) -> this.building = building,
                this
        );
    }

    private void updateRoom(int row) {
        Long roomId = (Long) roomTableModel.getValueAt(row, 0);
        roomService.getRoomById(
                roomId,
                (room) -> new UpdateBasicRoomDialog(null, room, roomService, () -> {
                    updateRoomTable();
                    if (onBuildingUpdated != null) {
                        onBuildingUpdated.run();
                    }
                    updateBuilding();
                }),
                null
        );
    }

    private void deleteRoom(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this room?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == YES_OPTION) {  // TODO: update roomList after adding/deleteing
            Long roomId = (Long) roomTableModel.getValueAt(row, 0);
            roomService.getRoomById(
                    roomId,
                    (room) -> roomService.deleteRoomWithId(
                            room.roomId(),
                            (result) -> {
                                updateRoomTable();  // TODO: maybe in future repair it!
                                onBuildingUpdated.run();
                                dispose();
                            },
                            this
                    ),
                    null
            );
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(46, 204, 113));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(39, 174, 96), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(39, 174, 96)); // Darker green
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(46, 204, 113)); // Default green
            }
        });

        return button;
    }
}
