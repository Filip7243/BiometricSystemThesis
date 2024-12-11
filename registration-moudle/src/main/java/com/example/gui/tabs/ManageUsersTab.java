package com.example.gui.tabs;

import com.example.client.*;
import com.example.client.dto.UserDTO;
import com.example.gui.BasePanel;
import com.example.gui.tabs.tables.MyTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;

public class ManageUsersTab extends BasePanel implements ActionListener {

    private final UserClient userClient = new UserClient();
    private final BuildingClient buildingClient = new BuildingClient();
    private final RoomClient roomClient = new RoomClient();
    private final List<UserDTO> users = new ArrayList<>();
    private final UserService userService = new UserService(userClient);
    private final BuildingService buildingService = new BuildingService(buildingClient, roomClient);
    private final RoomService roomService = new RoomService(roomClient);

    private DefaultTableModel userTableModel;
    private MyTable userTable;

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        // Create modern header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240)); // Light gray modern background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Add padding

        JLabel headerLabel = new JLabel("Manage Users", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(new Color(52, 73, 94)); // Dark blue color
        headerLabel.setBorder(new EmptyBorder(20, 10, 20, 10));

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        headerPanel.add(separator, BorderLayout.CENTER);

        // Create button panel (nav bar style)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Horizontal alignment
        buttonPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        // Create and style buttons
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(this);
        styleButton(btnRefresh, new Color(23, 162, 184), 150, 40);

        buttonPanel.add(btnRefresh);

        headerPanel.add(headerLabel, NORTH);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Define columns for the table
        String[] columns = {"ID", "First Name", "Last Name", "PESEL", "Role", "Edit", "Delete", "Details"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Initialize table
        userTable = new MyTable(userTableModel);

        // Center align cell renderers for specific columns
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 4; i++) {
            userTable.setColumnRenderer(i, defaultTableCellRenderer);
        }

        // Set custom button renderers for action columns
        userTable.setColumnRenderer(5, new ButtonRenderer("Edit"));
        userTable.setColumnRenderer(6, new ButtonRenderer("Delete"));
        userTable.setColumnRenderer(7, new ButtonRenderer("Details"));

        // Add mouse listener for button actions
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int column = userTable.columnAtPoint(e.getPoint());

                if (row >= 0 && row < userTable.getRowCount() && column >= 0 && column < userTable.getColumnCount()) {
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

        // Add toolbar with actions
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(240, 240, 240)); // Match header background


        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultValues();
    }

    @Override
    protected void setDefaultValues() {
        userService.getAllUsers(
                (result) -> {
                    users.clear();
                    users.addAll(result);
                    updateTableData();
                },
                this
        );
    }

    @Override
    protected void updateControls() {

    }

    @Override
    protected void updateFingersTools() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Refresh Data")) {
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
        new EditUserDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                user,
                userService,
                (result) -> setDefaultValues()
        );
    }

    private void handleDeleteUser(UserDTO user) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete user " + user.firstName() + " " + user.lastName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            userService.deleteUser(
                    user.id(),
                    (result) -> setDefaultValues(),
                    this
            );
        }
    }

    private void showUserDetails(UserDTO user) {
        new UserDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                user,
                userService,
                roomService,
                buildingService
        );
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

        button.addActionListener(this);
    }
}
