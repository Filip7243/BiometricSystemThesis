package com.example.gui;

import com.example.client.*;
import com.example.client.dto.UserDTO;
import com.example.gui.tables.MyTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.example.gui.StyledComponentFactory.createStyledTextField;

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

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel headerLabel = new JLabel("Manage Users", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(new Color(52, 73, 94));
        headerLabel.setBorder(new EmptyBorder(20, 10, 20, 10));

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        headerPanel.add(separator, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton btnRefresh = createStyledButton("Refresh Data", new Color(46, 204, 113), 150, 40);
        btnRefresh.addActionListener(this);

        buttonPanel.add(btnRefresh);

        JTextField searchBar = createStyledTextField("");
        searchBar.setPreferredSize(new Dimension(300, 40));

        JButton searchButton = createStyledButton("Search", new Color(52, 152, 219), 150, 40);
        searchButton.setEnabled(false);
        searchButton.addActionListener(e -> getUsers(searchBar.getText().trim()));

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            private void updateButtonState() {
                searchButton.setEnabled(!searchBar.getText().trim().isEmpty());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        });

        buttonPanel.add(searchBar);
        buttonPanel.add(searchButton);

        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "First Name", "Last Name", "PESEL", "Role", "Edit", "Delete", "Details"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new MyTable(userTableModel);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 4; i++) {
            userTable.setColumnRenderer(i, defaultTableCellRenderer);
        }

        userTable.setColumnRenderer(5, new ButtonRenderer("Edit"));
        userTable.setColumnRenderer(6, new ButtonRenderer("Delete"));
        userTable.setColumnRenderer(7, new ButtonRenderer("Details"));

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int column = userTable.columnAtPoint(e.getPoint());

                if (row >= 0 && row < userTable.getRowCount() && column >= 0 && column < userTable.getColumnCount()) {
                    if (column == 5) {
                        showEditUserDialog(users.get(row));
                    } else if (column == 6) {
                        handleDeleteUser(users.get(row));
                    } else if (column == 7) {
                        showUserDetails(users.get(row));
                    }
                }
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        setDefaultValues();
    }

    @Override
    protected void setDefaultValues() {
        getUsers("");
    }

    private void getUsers(String search) {
        userService.getAllUsers(
                search,
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
}
