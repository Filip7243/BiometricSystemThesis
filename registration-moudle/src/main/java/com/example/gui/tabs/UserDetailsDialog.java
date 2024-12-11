package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.RoomService;
import com.example.client.UserService;
import com.example.client.dto.FingerprintDTO;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UserDTO;
import com.example.gui.tabs.tables.MyTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.BorderLayout.*;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

public class UserDetailsDialog extends JDialog {

    private final UserDTO user;
    private final UserService userService;
    private final RoomService roomService;
    private final BuildingService buildingService;

    private DefaultTableModel roomModel;
    private DefaultTableModel fingerprintModel;

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
        setTitle("Details for user: " + user.firstName() + " " + user.lastName());
        setSize(850, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());


        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("User Details", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray
        headerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel headerDetails = new JLabel(
                "User: " + user.firstName() + " " + user.lastName() + " | PESEL: " + user.pesel(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray
        headerDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanelHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10)); // Horizontal alignment
        buttonPanelHeader.setBackground(new Color(240, 240, 240)); // Light gray background

        JButton addRoomsButton = new JButton("Add Rooms");
        styleButton(addRoomsButton, new Color(46, 204, 113), 150, 40);
        addRoomsButton.addActionListener(e -> addRoomToUser());  // You can implement the action as needed

        JButton refreshDataButton = new JButton("Refresh Data");
        styleButton(refreshDataButton, new Color(23, 162, 184), 150, 40);
        refreshDataButton.addActionListener(e -> refreshData()); // Placeholder for refreshing data logic

        buttonPanelHeader.add(addRoomsButton);
        buttonPanelHeader.add(refreshDataButton);

        headerPanel.add(headerTitle);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(headerDetails);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(buttonPanelHeader);

        // Fingerprints Panel
        JPanel fingerprintsPanel = new JPanel(new BorderLayout());
        fingerprintsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Fingerprints",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));
        Object[] fingerprintTableCols = {"ID", "Finger Type", "Action"};
        fingerprintModel = new DefaultTableModel(fingerprintTableCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        MyTable fingerprintTable = new MyTable(fingerprintModel);

        DefaultTableCellRenderer defaultFingerprintTableCellRenderer = new DefaultTableCellRenderer();
        defaultFingerprintTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        fingerprintTable.setColumnRenderer(0, defaultFingerprintTableCellRenderer);
        fingerprintTable.setColumnRenderer(1, defaultFingerprintTableCellRenderer);
        fingerprintTable.setColumnRenderer(2, new ButtonRenderer("Details"));

        fingerprintTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fingerprintTable.rowAtPoint(e.getPoint());
                int column = fingerprintTable.columnAtPoint(e.getPoint());

                if (row < fingerprintTable.getRowCount() && row >= 0 &&
                        column < fingerprintTable.getColumnCount() && column >= 0) {
                    if (column == 2) {
                        System.out.println("ESSUNIA");
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

        // Rooms Panel
        JPanel roomsPanel = new JPanel(new BorderLayout());
        roomsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),  // Set border color and thickness
                "Assigned Rooms",                                          // Title of the border
                TitledBorder.DEFAULT_JUSTIFICATION,                        // Title alignment (left by default)
                TitledBorder.DEFAULT_POSITION,                             // Title position (top by default)
                new Font("Segoe UI", Font.BOLD, 14),                        // Set font to bold and increase size
                new Color(52, 73, 94)                                      // Set title color (dark blue)
        ));

        Object[] roomsTableColumns = {"ID", "Room Number", "Floor", "Detach User"};
        roomModel = new DefaultTableModel(roomsTableColumns, 0);
        MyTable roomTable = new MyTable(roomModel);

        DefaultTableCellRenderer defaultRoomTableCellRenderer = new DefaultTableCellRenderer();
        defaultRoomTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        roomTable.setColumnRenderer(0, defaultRoomTableCellRenderer);
        roomTable.setColumnRenderer(1, defaultRoomTableCellRenderer);
        roomTable.setColumnRenderer(2, defaultRoomTableCellRenderer);
        roomTable.setColumnRenderer(3, new ButtonRenderer("Delete"));

        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = roomTable.rowAtPoint(e.getPoint());
                int column = roomTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0 &&
                        column < roomTable.getColumnCount() && column >= 0) {
                    if (column == 3) {
                        Long roomId = (Long) roomModel.getValueAt(row, 0);
                        int confirm = JOptionPane.showConfirmDialog(
                                UserDetailsDialog.this,
                                "Are you sure you want to detach this user from the room?",
                                "Warning",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        if (confirm == JOptionPane.YES_OPTION) {
                            userService.detachUserFromRoom(
                                    user.id(),
                                    roomId,
                                    (result) -> getUserRooms(),
                                    UserDetailsDialog.this
                            );
                        } else {
                            System.out.println("Action canceled. User not detached from the room.");
                        }
                    }
                }
            }
        });

        getUserRooms();

        roomsPanel.add(new JScrollPane(roomTable), CENTER);

        // Split pane for layouts
        JSplitPane splitPane = new JSplitPane(
                VERTICAL_SPLIT,
                fingerprintsPanel,
                roomsPanel
        );
        splitPane.setDividerLocation(200);

        mainPanel.add(splitPane, CENTER);

        // Adding header panel to the main panel
        mainPanel.add(headerPanel, NORTH);

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
                    roomModel.setRowCount(0);
                    for (RoomDTO room : rooms) {
                        roomModel.addRow(new Object[]{
                                room.roomId(),
                                room.roomNumber(),
                                room.floor(),
                                "Detach User"
                        });
                    }
                },
                this
        );
    }

    private void getUserFingerprints() {
        userService.getUserFingerprints(
                user.id(),
                (fingerprints) -> {
                    fingerprintModel.setRowCount(0);
                    for (FingerprintDTO fingerprint : fingerprints) {
                        fingerprintModel.addRow(new Object[]{
                                fingerprint.id(),
                                fingerprint.fingerType(),
                                "Details"
                        });
                    }
                },
                this
        );
    }

    private void refreshData() {
        getUserRooms();
        getUserFingerprints();
    }

    private void styleButton(JButton button, Color backgroundColor, int width, int height) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bigger font size for buttons
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(getPredefinedCursor(HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height)); // Set button size

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
    }
}
