package com.example.gui.tabs;

import com.example.client.BuildingClient;
import com.example.client.RoomClient;
import com.example.client.UserClient;
import com.example.client.dto.*;
import com.example.gui.BasePanel;
import com.example.model.Role;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Font.BOLD;

public class ManageUsersTab extends BasePanel implements ActionListener {

    private final UserClient userClient = new UserClient();
    private final BuildingClient buildingClient = new BuildingClient();
    private final RoomClient roomClient = new RoomClient();
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private final List<UserDTO> users = new ArrayList<>();
    private JTable roomTable;

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        String[] columns = {"ID", "First Name", "Last Name", "PESEL", "Role", "Edit", "Delete", "Details"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(userTableModel);
        userTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Edit"));
        userTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Delete"));
        userTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Details"));

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int column = userTable.columnAtPoint(e.getPoint());

                if (row < userTable.getRowCount() && row >= 0 &&
                        column < userTable.getColumnCount() && column >= 0) {
                    if (column == 5) { // Edit button
                        showEditUserDialog(users.get(row));
                    } else if (column == 6) { // Delete button
                        handleDeleteUser(users.get(row));
                    } else if (column == 7) { // Details button
                        showUserDetails(users.get(row));
                    }
                }
            }
        });

        userTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                userTable.setCursor(getPredefinedCursor(HAND_CURSOR));
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(this);

        toolBar.add(btnRefresh);

        add(toolBar, NORTH);
        add(new JScrollPane(userTable), CENTER);

        setDefaultValues();
        System.out.println("ESSA!");
    }

    @Override
    protected void setDefaultValues() {
        SwingWorker<List<UserDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDTO> doInBackground() {
                return userClient.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    users.clear();
                    users.addAll(get());
                    updateTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageUsersTab.this,
                            "Error loading users: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    @Override
    protected void updateControls() {

    }

    @Override
    protected void updateFingersTools() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Refresh")) {
            setDefaultValues();
        }
    }

    private void updateTableData() {
        while (userTableModel.getRowCount() > 0) {
            userTableModel.removeRow(0);
        }

        for (UserDTO user : users) {
            userTableModel.addRow(new Object[]{
                    user.id(),
                    user.firstName(),
                    user.lastName(),
                    user.pesel(),
                    user.role(),
                    "Edit",
                    "Delete",
                    "Details"
            });
        }
    }

    private void showEditUserDialog(UserDTO user) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add input fields with existing values
        JTextField firstNameField = new JTextField(user.firstName(), 20);
        JTextField lastNameField = new JTextField(user.lastName(), 20);
        JTextField peselField = new JTextField(user.pesel(), 20);
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        roleCombo.setSelectedItem(user.role());

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("PESEL:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(peselField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(roleCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            UpdateUserRequest request = new UpdateUserRequest(
                    user.id(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    peselField.getText().trim(),
                    ((Role) Objects.requireNonNull(roleCombo.getSelectedItem())).name()
            );

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    userClient.updateUser(request);

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        setDefaultValues();
                        JOptionPane.showMessageDialog(
                                ManageUsersTab.this,
                                "User updated successfully",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Error updating user: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(mainPanel, CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleDeleteUser(UserDTO user) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete user " + user.firstName() + " " + user.lastName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    userClient.deleteUser(user.id());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        setDefaultValues();
                        JOptionPane.showMessageDialog(
                                ManageUsersTab.this,
                                "User deleted successfully",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ManageUsersTab.this,
                                "Error deleting user: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void showUserDetails(UserDTO user) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "User Details - " + user.firstName() + " " + user.lastName(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create fingerprints panel
        JPanel fingerprintsPanel = new JPanel(new BorderLayout());
        fingerprintsPanel.setBorder(BorderFactory.createTitledBorder("Fingerprints"));

        DefaultTableModel fingerprintModel = new DefaultTableModel(
                new Object[]{"ID", "Finger Type", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        JTable fingerprintTable = new JTable(fingerprintModel);
        fingerprintTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer("View"));
        fingerprintTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), user));


        for (FingerprintDTO fingerprint : user.fingerprints()) {
            fingerprintModel.addRow(new Object[]{
                    fingerprint.id(),
                    fingerprint.fingerType(),
                    "View"
            });
        }

        fingerprintsPanel.add(new JScrollPane(fingerprintTable), CENTER);

        // Create assigned rooms panel
        JPanel roomsPanel = new JPanel(new BorderLayout());
        roomsPanel.setBorder(BorderFactory.createTitledBorder("Assigned Rooms"));

        DefaultTableModel roomModel = new DefaultTableModel(
                new Object[]{"ID", "Room Number", "Floor", "Detach User"}, 0);
        roomTable = new JTable(roomModel);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Delete"));

        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = roomTable.rowAtPoint(e.getPoint());
                int column = roomTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0 &&
                        column < roomTable.getColumnCount() && column >= 0) {
                    if (column == 3) {
                        Long roomId = (Long) roomModel.getValueAt(row, 0);
                        detachUserFromRoom(user.id(), roomId);
                    }
                }
            }
        });

        new SwingWorker<List<RoomDTO>, Void>() {
            @Override
            protected List<RoomDTO> doInBackground() {
                return userClient.getUserRooms(user.id());
            }

            @Override
            protected void done() {
                try {
                    List<RoomDTO> rooms = get();
                    for (RoomDTO room : rooms) {
                        roomModel.addRow(new Object[]{
                                room.roomId(),
                                room.roomNumber(),
                                room.floor(),
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageUsersTab.this,
                            "Error loading user rooms: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();

        roomsPanel.add(new JScrollPane(roomTable), CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                fingerprintsPanel,
                roomsPanel
        );
        splitPane.setDividerLocation(200);

        mainPanel.add(splitPane, CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JButton addRoomsButton = new JButton("Add Rooms");
        addRoomsButton.addActionListener(e -> addRoomToUser(user));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        buttonPanel.add(addRoomsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void addRoomToUser(UserDTO user) {
        JDialog assignRoomDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Assign Rooms to " + user.firstName() + " " + user.lastName(),
                true
        );
        assignRoomDialog.setSize(800, 600);
        assignRoomDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create table model for buildings and rooms
        DefaultTableModel buildingRoomModel = new DefaultTableModel(
                new Object[]{"Building", "Building ID", "Room ID", "Room Number", "Floor", "Assign"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable buildingRoomTable = new JTable(buildingRoomModel);

        // Custom renderer and editor for the "Assign" column
        buildingRoomTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Assign"));

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
                        assignUserToRoom(user.id(), roomId, assignRoomDialog);
                    }
                }
            }
        });

        // Load buildings and rooms
        new SwingWorker<List<BuildingDTO>, Void>() {
            @Override
            protected List<BuildingDTO> doInBackground() {
                System.out.println("Loading buidlings...");
                return buildingClient.getAllBuildingsNotAssignedToUser(user.id());
            }

            @Override
            protected void done() {
                try {
                    System.out.println("Im done!");
                    List<BuildingDTO> buildings = get();
                    System.out.println(buildings);
                    Set<Long> assignedRoomIds = userClient.getUserRooms(user.id())
                            .stream()
                            .map(RoomDTO::roomId)
                            .collect(Collectors.toSet());

                    for (BuildingDTO building : buildings) {
                        for (RoomDTO room : building.rooms()) {
                            if (!assignedRoomIds.contains(room.roomId())) {
                                buildingRoomModel.addRow(new Object[]{
                                        building.buildingNumber(),
                                        building.id(),
                                        room.roomId(),
                                        room.roomNumber(),
                                        room.floor(),
                                        "Assign"
                                });
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageUsersTab.this,
                            "Error loading buildings and rooms: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();

        // Table with scrollpane
        JScrollPane scrollPane = new JScrollPane(buildingRoomTable);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(buildingRoomModel);
        buildingRoomTable.setRowSorter(sorter);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> assignRoomDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        assignRoomDialog.add(mainPanel);
        assignRoomDialog.setVisible(true);
    }

    // TODO: refactor code, zamowic malinke dobra, pozniej filtrowanie na kazdej tabeli i srotowanie po kolumnach, filtorwanie danych np. checkbox show only assigned rooms etc
    // TODO: ladniejsze gui, dporacowac, dodac grafiki, dodac walidacje danych lepsza etc...
    // TODO: wysylanie requesta z malinki na backend, identyfikacja odciksu i response do malinki

    private void assignUserToRoom(Long userId, Long roomId, JDialog parentDialog) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                System.out.println("I'm assigning room " + roomId + " to user " + userId);
                roomClient.assignRoomToUser(roomId, userId); // Assume this method exists
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    DefaultTableModel roomModel = (DefaultTableModel) roomTable.getModel();
                    roomModel.setRowCount(0);

                    new SwingWorker<List<RoomDTO>, Void>() {
                        @Override
                        protected List<RoomDTO> doInBackground() {
                            return userClient.getUserRooms(userId);
                        }

                        @Override
                        protected void done() {
                            try {
                                List<RoomDTO> rooms = get();
                                System.out.println("REFRESHED USER ROOMS: " + rooms);
                                for (RoomDTO room : rooms) {
                                    roomModel.addRow(new Object[]{
                                            room.roomId(),
                                            room.roomNumber(),
                                            room.floor()
                                    });
                                }

                                // Close the assign room dialog
                                parentDialog.dispose();
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                        ManageUsersTab.this,
                                        "Error refreshing user rooms: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                ex.printStackTrace();
                            }
                        }
                    }.execute();

                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(
                            ManageUsersTab.this,
                            "Error assigning user to room: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void detachUserFromRoom(Long userId, Long roomId) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                userClient.detachUserFromRoom(userId, roomId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    DefaultTableModel roomModel = (DefaultTableModel) roomTable.getModel();
                    roomModel.setRowCount(0);

                    new SwingWorker<List<RoomDTO>, Void>() {
                        @Override
                        protected List<RoomDTO> doInBackground() {
                            return userClient.getUserRooms(userId);
                        }

                        @Override
                        protected void done() {
                            try {
                                List<RoomDTO> rooms = get();
                                for (RoomDTO room : rooms) {
                                    roomModel.addRow(new Object[]{
                                            room.roomId(),
                                            room.roomNumber(),
                                            room.floor()
                                    });
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                        ManageUsersTab.this,
                                        "Error refreshing user rooms: " + ex.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                ex.printStackTrace();
                            }
                        }
                    }.execute();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
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

    private class ButtonEditor extends DefaultCellEditor {
        private final UserDTO user;
        private JButton button;
        private FingerprintDTO currentFingerprint;

        public ButtonEditor(JCheckBox checkBox, UserDTO user) {
            super(checkBox);
            this.user = user;

            button = new JButton("View");
            button.addActionListener(e -> {
                if (currentFingerprint != null) {
                    new FingerprintViewDialog(
                            (Frame) SwingUtilities.getWindowAncestor(ManageUsersTab.this),
                            currentFingerprint,
                            userClient
                    ).setVisible(true);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentFingerprint = user.fingerprints().get(row);
            return button;
        }
    }
}
