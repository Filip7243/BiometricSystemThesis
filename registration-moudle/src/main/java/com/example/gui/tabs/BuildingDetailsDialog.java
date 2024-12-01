package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomClient;
import com.example.client.RoomService;
import com.example.client.dto.AddRoomRequest;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.gui.ScannersListPanel;

import javax.swing.*;
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
    private final List<BuildingDTO> buildings;

    private BuildingDTO building;
    private DefaultTableModel roomTableModel;

    public BuildingDetailsDialog(Frame owner,
                                 BuildingDTO building,
                                 BuildingService buildingService,
                                 Runnable onBuildingUpdated,
                                 List<BuildingDTO> buildings) {
        super(owner, true);

        this.building = building;
        this.buildingService = buildingService;
        this.onBuildingUpdated = onBuildingUpdated;
        this.buildings = buildings;

        initComponent();
    }

    private void initComponent() {
        setTitle("Details for building " + building.buildingNumber());
        setSize(600, 400);
        setLocationRelativeTo(null);

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

        JPanel buttonPanel = new JPanel(new FlowLayout(RIGHT));
        JButton addRoomButton = new JButton("Add Room");

        addRoomButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(addRoomButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoPanel, WEST);
        topPanel.add(buttonPanel, EAST);

        panel.add(topPanel, NORTH);

        String[] columnNames = {"Room ID", "Room Number", "Floor", "Update", "Delete", "Set Device"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable roomTable = new JTable(roomTableModel);
        roomTable.setShowGrid(true);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(CENTER);
        roomTable.getColumnModel().getColumn(0).setCellRenderer(defaultTableCellRenderer);
        roomTable.getColumnModel().getColumn(1).setCellRenderer(defaultTableCellRenderer);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(defaultTableCellRenderer);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Edit"));
        roomTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Delete"));
        roomTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Details"));

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
        //TODO: manage user details detach room refresh table doesnt work
        addRoomButton.addActionListener(e -> {
            new AddOrUpdateRoomInBuildingDialog(
                    (Frame) getParent(),
                    true,
                    (request) -> buildingService.addRoomToBuilding(
                            new AddRoomRequest(request.roomNumber(), request.floor(), building.id(), request.deviceHardwareId()),
                            (room) -> {
                                onBuildingUpdated.run();
                                updateRoomTable();
                                updateBuilding();
                            },
                            this),
                    null
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

        roomTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                roomTable.setCursor(getPredefinedCursor(HAND_CURSOR));
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
                JOptionPane.YES_NO_OPTION
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
}
