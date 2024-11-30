package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomService;
import com.example.client.UserService;
import com.example.client.dto.FingerprintDTO;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UserDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.BorderLayout.CENTER;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

public class UserDetailsDialog extends JDialog {

    private final UserDTO user;
    private final UserService userService;
    private final RoomService roomService;
    private final BuildingService buildingService;

    private DefaultTableModel roomModel;

    public UserDetailsDialog(Frame parent, UserDTO user, UserService userService,
                             RoomService roomService, BuildingService buildingService) {
        super(parent, true);

        this.user = user;
        this.userService = userService;
        this.roomService = roomService;
        this.buildingService = buildingService;

        initComponents();
    }

    private void initComponents() {
        setTitle("User: " + user.firstName() + " " + user.lastName() + " details");
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel fingerprintsPanel = new JPanel(new BorderLayout());
        fingerprintsPanel.setBorder(BorderFactory.createTitledBorder("Fingerprints"));
        DefaultTableModel fingerprintModel = new DefaultTableModel(
                new Object[]{"ID", "Finger Type", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable fingerprintTable = new JTable(fingerprintModel);
        fingerprintTable.getColumnModel()
                .getColumn(2)
                .setCellRenderer(new ButtonRenderer("Details"));
        fingerprintTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fingerprintTable.rowAtPoint(e.getPoint());
                int column = fingerprintTable.columnAtPoint(e.getPoint());

                if (row < fingerprintTable.getRowCount() && row >= 0 &&
                        column < fingerprintTable.getColumnCount() && column >= 0) {
                    if (column == 2) {
                        Long fingerprintId = (Long) fingerprintModel.getValueAt(row, 0);
                        new FingerprintViewDialog(
                                (Frame) getParent(),
                                user.fingerprints().get(row),
                                userService // TODO: zmienic ten row w fingerptins
                        );
                    }
                }
            }
        });

        for (FingerprintDTO fingerprint : user.fingerprints()) {
            fingerprintModel.addRow(new Object[]{
                    fingerprint.id(),
                    fingerprint.fingerType(),
                    "Details"
            });
        }

        fingerprintsPanel.add(new JScrollPane(fingerprintTable), CENTER);

        JPanel roomsPanel = new JPanel(new BorderLayout());
        roomsPanel.setBorder(BorderFactory.createTitledBorder("Assigned Rooms"));

        roomModel = new DefaultTableModel(
                new Object[]{"ID", "Room Number", "Floor", "Detach User"}, 0);
        JTable roomTable = new JTable(roomModel);
        roomTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(new ButtonRenderer("Delete"));
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = roomTable.rowAtPoint(e.getPoint());
                int column = roomTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0 &&
                        column < roomTable.getColumnCount() && column >= 0) {
                    if (column == 3) {
                        Long roomId = (Long) roomModel.getValueAt(row, 0);
                        userService.detachUserFromRoom(
                                user.id(),
                                roomId,
                                (result) -> {
                                    getUserRooms();
                                },
                                UserDetailsDialog.this
                        );
                    }
                }
            }
        });

        getUserRooms();

        roomsPanel.add(new JScrollPane(roomTable), CENTER);

        JSplitPane splitPane = new JSplitPane(
                VERTICAL_SPLIT,
                fingerprintsPanel,
                roomsPanel
        );
        splitPane.setDividerLocation(200);

        mainPanel.add(splitPane, CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JButton addRoomsButton = new JButton("Add Rooms");
        addRoomsButton.addActionListener(e -> addRoomToUser());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        buttonPanel.add(addRoomsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void addRoomToUser() {
        new AddRoomToUserDialog(
                (Frame) getParent(),
                user,
                userService,
                buildingService,
                roomService,
                result -> {
                    roomModel.setRowCount(0);
                    getUserRooms();
                }
        );
    }

    private void getUserRooms() {
        userService.getUserRooms(
                user.id(),
                (rooms) -> {
                    for (RoomDTO room : rooms) {
                        roomModel.addRow(new Object[]{
                                room.roomId(),
                                room.roomNumber(),
                                room.floor(),
                        });
                    }
                },
                this
        );
    }
}
