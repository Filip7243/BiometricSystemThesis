package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomService;
import com.example.client.UserService;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UserDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.awt.BorderLayout.CENTER;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

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

        DefaultTableModel buildingRoomModel = new DefaultTableModel(
                new Object[]{"Building", "Building ID", "Room ID", "Room Number", "Floor", "Assign"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable buildingRoomTable = new JTable(buildingRoomModel);
        buildingRoomTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Edit"));
        buildingRoomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = buildingRoomTable.rowAtPoint(e.getPoint());
                int column = buildingRoomTable.columnAtPoint(e.getPoint());

                if (row >= 0 && row < buildingRoomTable.getRowCount() &&
                        column >= 0 && column < buildingRoomTable.getColumnCount()) {
                    if (column == 5) { // "Assign" column
                        Long roomId = (Long) buildingRoomModel.getValueAt(row, 2);
                        System.out.println("Assigning room " + roomId + " to user " + user.id());
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

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
                    INFORMATION_MESSAGE);
            dispose();
        }, this);
    }
}
