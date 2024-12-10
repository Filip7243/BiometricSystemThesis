package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomService;
import com.example.client.UserService;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UserDTO;
import com.example.gui.tabs.tables.MyTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.awt.BorderLayout.CENTER;

public class AddRoomToUserDialog extends JDialog {

    private final UserDTO user;
    private final UserService userService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final Consumer<Void> refreshRoomTableCallback;

    public AddRoomToUserDialog(Frame owner, UserDTO user,
                               UserService userService, BuildingService buildingService, RoomService roomService,
                               Consumer<Void> refreshRoomTableCallback) {
        super(owner, true);

        this.user = user;
        this.userService = userService;
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.refreshRoomTableCallback = refreshRoomTableCallback;

        initComponents();
    }

    private void initComponents() {
        setTitle("Assign Rooms to " + user.firstName() + " " + user.lastName());
        setSize(800, 600);
        setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Assign Rooms to " + user.firstName() + " " + user.lastName(), SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(new Color(52, 73, 94)); // Dark blue color
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Add spacing around the header label
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table model and setup
        DefaultTableModel buildingRoomModel = new DefaultTableModel(
                new Object[]{"Building", "Building ID", "Room ID", "Room Number", "Floor", "Assign"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        MyTable buildingRoomTable = new MyTable(buildingRoomModel);
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 4; i++) {
            buildingRoomTable.setColumnRenderer(i, defaultTableCellRenderer);
        }
        buildingRoomTable.setColumnRenderer(5, new ButtonRenderer("Edit"));

        buildingRoomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = buildingRoomTable.rowAtPoint(e.getPoint());
                int column = buildingRoomTable.columnAtPoint(e.getPoint());

                if (row >= 0 && row < buildingRoomTable.getRowCount() &&
                        column >= 0 && column < buildingRoomTable.getColumnCount()) {
                    if (column == 5) { // "Assign" column
                        Long roomId = (Long) buildingRoomModel.getValueAt(row, 2);
                        assignUserToRoom(user.id(), roomId);
                    }
                }
            }
        });

        buildingService.getAllBuildingsNotAssignedToUser(user.id(), buildings -> {
            userService.getUserRooms(user.id(), rooms -> {
                Set<Long> assignedRoomIds = rooms.stream()
                        .map(RoomDTO::roomId)
                        .collect(Collectors.toSet());

                buildings.forEach(building ->
                        building.rooms().stream()
                                .filter(room -> !assignedRoomIds.contains(room.roomId()))
                                .forEach(room ->
                                        buildingRoomModel.addRow(new Object[]{
                                                building.buildingNumber(),
                                                building.id(),
                                                room.roomId(),
                                                room.roomNumber(),
                                                room.floor(),
                                                "Assign"
                                        })
                                )
                );
            }, this);
        }, this);

        JScrollPane scrollPane = new JScrollPane(buildingRoomTable);
        mainPanel.add(scrollPane, CENTER);

        // Remove close button and do not add any other buttons in the dialog

        add(mainPanel);
        setVisible(true);
    }

    private void assignUserToRoom(Long userId, Long roomId) {
        roomService.assignRoomToUser(roomId, userId, user -> {
            refreshRoomTableCallback.accept(null);
            JOptionPane.showMessageDialog(
                    this,
                    "Room assigned successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }, this);
    }
}
