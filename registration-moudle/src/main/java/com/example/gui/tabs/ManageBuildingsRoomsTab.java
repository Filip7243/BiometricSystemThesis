package com.example.gui.tabs;

import com.example.client.BuildingClient;
import com.example.client.BuildingService;
import com.example.client.RoomClient;
import com.example.client.dto.BuildingDTO;
import com.example.gui.BasePanel;
import com.example.gui.tabs.tables.MyTable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.YES_OPTION;

public class ManageBuildingsRoomsTab extends BasePanel implements ActionListener {

    private final List<BuildingDTO> buildings = new ArrayList<>();
    private final BuildingClient buildingClient = new BuildingClient();
    private final RoomClient roomClient = new RoomClient();

    private DefaultTableModel buildingTableModel;
    private MyTable buildingTable;

    private JButton addBuildingButton;
    private JButton btnRefreshData;
    private JButton searchButton;

    private BuildingService buildingService;

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        buildingService = new BuildingService(buildingClient, roomClient);

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Manage Buildings And Rooms", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(new Color(52, 73, 94)); // Dark blue color
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        headerPanel.add(separator, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        addBuildingButton = new JButton("Add Building");
        styleButton(addBuildingButton, new Color(46, 204, 113), 150, 40);

        btnRefreshData = new JButton("Refresh Data");
        styleButton(btnRefreshData, new Color(23, 162, 184), 150, 40);

        buttonPanel.add(addBuildingButton);
        buttonPanel.add(btnRefreshData);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 10));
        searchPanel.setBackground(new Color(240, 240, 240));

        JTextField searchBar = createStyledTextField("");
        searchBar.setPreferredSize(new Dimension(300, 40));

        searchButton = new JButton("Search");
        styleButton(searchButton, new Color(52, 152, 219), 150, 40);
        searchButton.setEnabled(false);
        searchButton.addActionListener(e -> getBuildings(searchBar.getText().trim()));

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

        searchPanel.add(searchBar);
        searchPanel.add(searchButton);

        buttonPanel.add(searchPanel);

        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        createBuildingTable();
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void setDefaultValues() {
        addBuildingButton.setEnabled(false);
        buildingTable.setEnabled(false);

        getBuildings("");
    }

    private void getBuildings(String search) {
        buildingService.getAllBuildings(
                search,
                buildings -> {
                    this.buildings.clear();
                    this.buildings.addAll(buildings);
                    buildingTableModel.setRowCount(0);
                    for (BuildingDTO building : buildings) {
                        buildingTableModel.addRow(new Object[]{
                                building.id(),
                                building.buildingNumber(),
                                building.street(),
                                "Edit",
                                "Delete",
                                "Details"
                        });
                    }

                    addBuildingButton.setEnabled(true);
                    buildingTable.setEnabled(true);
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
        if (e.getSource().equals(addBuildingButton)) {
            showAddBuildingDialog();
        } else if (e.getSource().equals(btnRefreshData)) {
            setDefaultValues();
        }
    }

    private void createBuildingTable() {
        String[] cols = {"ID", "Building Number", "Street", "Edit", "Delete", "Rooms"};

        buildingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        buildingTable = new MyTable(buildingTableModel);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        buildingTable.setColumnRenderer(0, defaultTableCellRenderer);
        buildingTable.setColumnRenderer(1, defaultTableCellRenderer);
        buildingTable.setColumnRenderer(2, defaultTableCellRenderer);
        buildingTable.setColumnRenderer(3, new ButtonRenderer("Edit"));
        buildingTable.setColumnRenderer(4, new ButtonRenderer("Delete"));
        buildingTable.setColumnRenderer(5, new ButtonRenderer("Details"));

        buildingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = buildingTable.rowAtPoint(e.getPoint());
                int col = buildingTable.columnAtPoint(e.getPoint());

                if (row < buildingTable.getRowCount() && row >= 0 && col < buildingTable.getColumnCount() && col >= 0) {
                    if (col == 3) {
                        editBuilding(row);
                    } else if (col == 4) {
                        deleteBuilding(row);
                    } else if (col == 5) {
                        showBuildingDetails(row);
                    }
                }
            }
        });
    }

    private void showAddBuildingDialog() {
        new AddBuildingDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                buildingService,
                newBuilding -> updateBuildingTable()
        );
    }

    private void updateBuildingTable() {
        buildingService.getAllBuildings(
                "",
                buildings -> {
                    this.buildings.clear();
                    this.buildings.addAll(buildings);
                    buildingTableModel.setRowCount(0);
                    for (BuildingDTO building : buildings) {
                        buildingTableModel.addRow(new Object[]{
                                building.id(),
                                building.buildingNumber(),
                                building.street(),
                                "Edit",
                                "Delete",
                                "Details"
                        });
                    }
                },
                this
        );
    }

    private void editBuilding(int row) {
        new EditBuildingDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                buildings.get(row),
                buildingService,
                this::updateBuildingTable
        );
    }

    private void deleteBuilding(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this building? The rooms will be deleted too!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == YES_OPTION) {
            BuildingDTO buildingToRemove = buildings.get(row);
            buildings.remove(row);

            buildingService.deleteBuildingWithId(
                    buildingToRemove.id(),
                    result -> {
                        updateBuildingTable();

                        JOptionPane.showMessageDialog(
                                null,
                                "Building as been deleted!",
                                "Success",
                                INFORMATION_MESSAGE
                        );
                    },
                    this
            );
        }
    }

    private void showBuildingDetails(int row) {
        BuildingDTO building = buildings.get(row);
        new BuildingDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                building,
                buildingService,
                () -> {
                    JOptionPane.showMessageDialog(null,
                            "Room added successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    updateBuildingTable();
                }
        );
    }

    private void styleButton(JButton button, Color backgroundColor, int width, int height) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Bigger font size for buttons
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(getPredefinedCursor(HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));

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

    private JTextField createStyledTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        textField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return textField;
    }
}
