package com.example.gui.tabs;

import com.example.client.*;
import com.example.client.dto.UserDTO;
import com.example.gui.BasePanel;

import javax.swing.*;
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
    private final BuildingService buildingService = new BuildingService(buildingClient);
    private final RoomService roomService = new RoomService(roomClient);

    private DefaultTableModel userTableModel;
    private JTable userTable;

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
                JOptionPane.YES_NO_OPTION
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

    // TODO: refactor code, zamowic malinke dobra, pozniej filtrowanie na kazdej tabeli i srotowanie po kolumnach, filtorwanie danych np. checkbox show only assigned rooms etc
    // TODO: ladniejsze gui, dporacowac, dodac grafiki, dodac walidacje danych lepsza etc...
    // TODO: wysylanie requesta z malinki na backend, identyfikacja odciksu i response do malinki
}
