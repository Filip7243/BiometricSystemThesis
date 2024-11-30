package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.BuildingClient;
import com.example.client.BuildingService;
import com.example.client.RoomClient;
import com.example.client.dto.*;
import com.example.gui.BasePanel;
import com.example.gui.ScannersListPanel;
import com.neurotec.devices.NFScanner;
import com.neurotec.devices.NFingerScanner;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.awt.BorderLayout.*;
import static java.awt.Cursor.*;
import static java.awt.FlowLayout.RIGHT;
import static java.awt.Font.BOLD;
import static javax.swing.JOptionPane.*;

public class ManageBuildingsRoomsTab extends BasePanel implements ActionListener {

    private final JFrame mainFrame;
    private final List<BuildingDTO> buildings = new ArrayList<>();
    private final List<RoomDTO> rooms = new ArrayList<>();
    private final BuildingClient buildingClient = new BuildingClient();
    private final RoomClient roomClient = new RoomClient();
    private JTable buildingTable;
    private JTable roomTable;
    private DefaultTableModel buildingTableModel;
    private DefaultTableModel roomTableModel;
    private JButton addBuildingButton;
    private JButton btnRefreshData;
    private ScannersListPanel scannersListPanel;
    private BuildingService buildingService;

    public ManageBuildingsRoomsTab(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        buildingService = new BuildingService(buildingClient);

        add(new JLabel("Buildings and Rooms Managing!"), NORTH);

        createBuildingTable();
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        add(scrollPane, CENTER);

        addBuildingButton = new JButton("Add Building");
        addBuildingButton.addActionListener(this);
        addBuildingButton.setCursor(getPredefinedCursor(HAND_CURSOR));

        btnRefreshData = new JButton("Refresh Data");
        btnRefreshData.addActionListener(this);
        btnRefreshData.setCursor(getPredefinedCursor(HAND_CURSOR));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBuildingButton);
        buttonPanel.add(btnRefreshData);

        add(buttonPanel, SOUTH);
    }

    @Override
    protected void setDefaultValues() {
        addBuildingButton.setEnabled(false);
        buildingTable.setEnabled(false);
        btnRefreshData.setEnabled(false);


        SwingWorker<List<BuildingDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BuildingDTO> doInBackground() {
                try {
                    return buildingClient.getAllBuildings();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<BuildingDTO> fetchedBuildings = get();
                    System.out.println(fetchedBuildings);
                    buildings.clear();
                    buildings.addAll(fetchedBuildings);
                    updateBuildingTable();

                    System.out.println("Buildings fetched: " + fetchedBuildings.size());
                    addBuildingButton.setEnabled(true);
                    buildingTable.setEnabled(true);
                    btnRefreshData.setEnabled(true);
                } catch (Exception e) {
                    System.out.println("Error fetching buildings: " + e.getMessage());
                    e.printStackTrace();
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
        buildingTable = new JTable(buildingTableModel);
        buildingTable.setShowGrid(true);
        buildingTable.setGridColor(Color.LIGHT_GRAY);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        buildingTable.getColumnModel().getColumn(0).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(1).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(2).setCellRenderer(defaultTableCellRenderer);
        buildingTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer("Edit"));
        buildingTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Delete"));
        buildingTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Details"));

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

        buildingTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                buildingTable.setCursor(getPredefinedCursor(HAND_CURSOR));
            }
        });
    }

    private void showAddBuildingDialog() {
        new AddBuildingDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                buildingService,
                buildingTableModel
        );
//        JDialog dialog = new JDialog(mainFrame, "Add Building", true);
//        dialog.setSize(400, 500);
//        dialog.setLocationRelativeTo(mainFrame);
//
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        JTextField buildingNumberField = new JTextField(20);
//        buildingNumberField.setCursor(getPredefinedCursor(TEXT_CURSOR));
//        JTextField streetField = new JTextField(20);
//        streetField.setCursor(getPredefinedCursor(TEXT_CURSOR));
//        DefaultListModel<RoomDTO> roomListModel = new DefaultListModel<>();
//        JList<RoomDTO> roomList = new JList<>(roomListModel);  //TODO: dodac usuwanie pokoju przy dodawaniu jakbym sie pomylil
//        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        panel.add(new JLabel("Building Number:"), gbc);
//        gbc.gridx = 1;
//        panel.add(buildingNumberField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        panel.add(new JLabel("Street:"), gbc);
//        gbc.gridx = 1;
//        panel.add(streetField, gbc);
//
//        JButton addRoomButton = new JButton("Add Room");
//        addRoomButton.setCursor(getPredefinedCursor(HAND_CURSOR));
//        addRoomButton.addActionListener(e -> {
//            String roomNumber = JOptionPane.showInputDialog("Enter Room Number:");
//            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
//                roomListModel.addElement(new RoomDTO((long) (roomListModel.size() + 1), roomNumber, 1, null));  // TODO: add device id
//            }
//        });
//
//        JButton removeRoomButton = new JButton("Remove Room");
//        removeRoomButton.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
//        removeRoomButton.addActionListener(e -> {
//            int selectedIndex = roomList.getSelectedIndex();
//            if (selectedIndex != -1) {
//                roomListModel.remove(selectedIndex); // Remove the selected room
//            } else {
//                JOptionPane.showMessageDialog(dialog, "Please select a room to remove.", "No Selection", WARNING_MESSAGE);
//            }
//        });
//
//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        gbc.gridwidth = 2;
//        panel.add(addRoomButton, gbc);
//
//        gbc.gridy = 3;
//        panel.add(removeRoomButton, gbc);
//
//        gbc.gridy = 4;
//        panel.add(new JScrollPane(roomList), gbc);
//
//        JPanel buttonPanel = new JPanel();
//        JButton saveButton = new JButton("Save");
//        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));
//        JButton clearButton = new JButton("Clear");
//        clearButton.setCursor(getPredefinedCursor(HAND_CURSOR));
//
//        saveButton.addActionListener(e -> {
//            List<RoomDTO> rooms = new ArrayList<>();
//            for (int i = 0; i < roomListModel.size(); i++) {
//                rooms.add(roomListModel.get(i));
//            }
//
//            BuildingDTO building = new BuildingDTO(
//                    (long) (buildings.size() + 1),
//                    buildingNumberField.getText(),
//                    streetField.getText(),
//                    rooms
//            );
//
//            buildings.add(building);
//            updateBuildingTable();
//            dialog.dispose();
//        });
//
//        clearButton.addActionListener(e -> {
//            buildingNumberField.setText("");
//            streetField.setText("");
//            roomListModel.clear();
//        });
//
//        buttonPanel.add(saveButton);
//        buttonPanel.add(clearButton);
//
//        gbc.gridy = 5;
//        panel.add(buttonPanel, gbc);
//
//        dialog.add(panel);
//        dialog.setVisible(true);
    }

    private void editBuilding(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog(mainFrame, "Edit Building", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField buildingNumberField = new JTextField(building.buildingNumber(), 20);
        buildingNumberField.setCursor(getPredefinedCursor(TEXT_CURSOR));
        JTextField streetField = new JTextField(building.street(), 20);
        streetField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblBuildingNumber = new JLabel("Building Number:");
        lblBuildingNumber.setFont(new Font("Arial", BOLD, 12));
        panel.add(lblBuildingNumber, gbc);
        gbc.gridx = 1;
        panel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblStreet = new JLabel("Street:");
        lblStreet.setFont(new Font("Arial", BOLD, 12));
        panel.add(lblStreet, gbc);
        gbc.gridx = 1;
        panel.add(streetField, gbc);

        // Add status label for feedback
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        saveButton.addActionListener(e -> {
            saveButton.setEnabled(false);
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("Updating building...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    UpdateBuildingRequest updateRequest = new UpdateBuildingRequest(
                            buildingNumberField.getText(),
                            streetField.getText()
                    );

                    // Call the API to update the building
                    System.out.println("Updating building with ID: " + building.id());
                    buildingClient.updateBuildingWithId(building.id(), updateRequest);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // This will throw an exception if there was an error in doInBackground

                        // Update local data
                        buildings.set(row, new BuildingDTO(
                                building.id(),
                                buildingNumberField.getText(),
                                streetField.getText(),
                                building.rooms()
                        ));
                        updateBuildingTable();

                        // Show success and close dialog
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Building updated successfully!",
                                "Success",
                                INFORMATION_MESSAGE
                        );
                        dialog.dispose();

                    } catch (Exception ex) {
                        // Handle error
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Error: " + ex.getMessage());
                        saveButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                                dialog,
                                "Failed to update building: " + ex.getMessage(),
                                "Error",
                                ERROR_MESSAGE
                        );
                    }
                }
            };

            worker.execute();
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteBuilding(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this building? The rooms will be deleted too!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            BuildingDTO buildingToRemove = buildings.get(row);
            buildings.remove(row);  // TODO: here remove buildings from db
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    try {
                        System.out.println("Deleting building with ID: " + buildingToRemove.id());
                        buildingClient.deleteBuildingWithId(buildingToRemove.id());
                    } catch (Exception e) {
                        System.out.println("Error deleting building: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        updateBuildingTable();
                        JOptionPane.showMessageDialog(
                                null,
                                "Building as been deleted!",
                                "Success",
                                INFORMATION_MESSAGE
                        );
                    } catch (InterruptedException | ExecutionException e) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Failed to update building: " + e.getMessage(),
                                "Error",
                                ERROR_MESSAGE
                        );

                    }
                }
            };

            worker.execute();
        }
    }

    private void showBuildingDetails(int row) {
        BuildingDTO building = buildings.get(row);
        JDialog dialog = new JDialog(mainFrame, "Building Details", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(mainFrame);

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
        roomTable = new JTable(roomTableModel);
        roomTable.setShowGrid(true);

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
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

        addRoomButton.addActionListener(e -> showAddRoomDialog(building, dialog));

        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = roomTable.rowAtPoint(e.getPoint());
                int col = roomTable.columnAtPoint(e.getPoint());

                if (row < roomTable.getRowCount() && row >= 0) {
                    if (col == 3) { // Update
                        updateRoom(building, row, dialog);
                    } else if (col == 4) { // Delete
                        deleteRoom(building, row, dialog);
                    } else if (col == 5) { // Set Device
                        showSetDeviceDialog(building, row, dialog);
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

        panel.add(new JScrollPane(roomTable), CENTER);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showAddRoomDialog(BuildingDTO building, JDialog parentDialog) {
        JDialog dialog = new JDialog(parentDialog, "Add New Room", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Room Number:"), gbc);

        gbc.gridx = 1;
        JTextField roomNumberField = new JTextField(15);
        inputPanel.add(roomNumberField, gbc);

        // Floor input
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Floor:"), gbc);

        gbc.gridx = 1;
        JSpinner floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        inputPanel.add(floorSpinner, gbc);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        ScannersListPanel scannersListPanel = new ScannersListPanel();
        scannersListPanel.hideFingersCombo();
        scannersListPanel.updateScannerList();
        mainPanel.add(scannersListPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                String roomNumber = roomNumberField.getText().trim();
                int floor = (Integer) floorSpinner.getValue();

                if (roomNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Room number cannot be empty",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                NFScanner selectedScanner = FingersTools.getInstance().getClient().getFingerScanner();
                String hardwareDeviceId = selectedScanner != null ? selectedScanner.getId() : null;

                saveButton.setEnabled(false);

                new SwingWorker<Integer, Void>() {
                    @Override
                    protected Integer doInBackground() {
                        RoomDTO createdRoom = roomClient.addRoom(new AddRoomRequest(roomNumber, floor, building.id(), hardwareDeviceId));

                        BuildingDTO updatedBuilding = buildingClient.getBuildingById(building.id());

                        int buildingIndex = buildings.indexOf(building);
                        buildings.set(buildingIndex, updatedBuilding);

                        return buildingIndex;
                    }

                    @Override
                    protected void done() {
                        try {
                            int buildingIndex = get();

                            dialog.dispose();
                            if (parentDialog != null) {
                                parentDialog.dispose();
                            }

                            JOptionPane.showMessageDialog(null,
                                    "Room added successfully",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);

                            showBuildingDetails(buildingIndex);

                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            showError("Operation interrupted");
                        } catch (ExecutionException ex) {
                            showError("Failed to add room: " + ex.getCause().getMessage());
                        } finally {
                            saveButton.setEnabled(true);
                        }
                    }

                    private void showError(String message) {
                        JOptionPane.showMessageDialog(dialog,
                                message,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }.execute();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showSetDeviceDialog(BuildingDTO building, int roomIndex, JDialog parentDialog) {
        RoomDTO room = building.rooms().get(roomIndex);

        JDialog deviceDialog = new JDialog(mainFrame, "Set Device for " + room.roomNumber(), true);
        deviceDialog.setSize(500, 300);
        deviceDialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel currentScannerPanel = new JPanel(new GridBagLayout());
        currentScannerPanel.setBorder(BorderFactory.createTitledBorder("Current Scanner"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel scannerLabel = new JLabel(room.hardwareDeviceId() != null ?
                "Scanner: " + room.hardwareDeviceId() :
                "No scanner assigned");
        scannerLabel.setFont(new Font("Arial", BOLD, 12));

        gbc.gridx = 0;
        gbc.gridy = 0;
        currentScannerPanel.add(scannerLabel, gbc);

        if (room.hardwareDeviceId() != null) {
            JButton removeButton = new JButton("Remove Scanner");
            removeButton.setCursor(getPredefinedCursor(HAND_CURSOR));
            removeButton.setBackground(new Color(255, 51, 0));
            removeButton.setForeground(Color.WHITE);
            removeButton.setFont(new Font("Arial", BOLD, 12));
            removeButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        deviceDialog,
                        "Are you sure you want to remove the scanner from this room?",
                        "Confirm Remove",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            roomClient.removeDeviceFromRoom(new AssignDeviceToRoomRequest(room.roomId(), room.hardwareDeviceId()));
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();

                                BuildingDTO updatedBuilding = buildingClient.getBuildingById(building.id());
                                int buildingIndex = buildings.indexOf(building);
                                buildings.set(buildingIndex, updatedBuilding);

                                JOptionPane.showMessageDialog(
                                        null,
                                        "Device has been removed from room " + room.roomNumber(),
                                        "Success",
                                        INFORMATION_MESSAGE
                                );

                                scannerLabel.setText("No scanner assigned");
                                removeButton.setVisible(false);
                                deviceDialog.pack();
                                deviceDialog.dispose();
                                if (parentDialog != null) {
                                    parentDialog.dispose();
                                }

                                showBuildingDetails(buildingIndex);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "Error while removing device: " + ex.getMessage(),
                                        "Error",
                                        ERROR_MESSAGE
                                );
                                ex.printStackTrace();
                            }
                        }
                    };

                    worker.execute();
                }
            });

            gbc.gridx = 1;
            gbc.weightx = 0.0;
            currentScannerPanel.add(removeButton, gbc);
        }

        mainPanel.add(currentScannerPanel, NORTH);

        scannersListPanel = new ScannersListPanel();
        scannersListPanel.hideFingersCombo();
        scannersListPanel.updateScannerList();
        mainPanel.add(scannersListPanel, CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(RIGHT, 5, 5));

        JButton assignButton = new JButton("Assign Scanner");
        assignButton.setEnabled(room.hardwareDeviceId() == null);
        assignButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        assignButton.setBackground(new Color(0, 128, 0));
        assignButton.setForeground(Color.WHITE);
        assignButton.setFont(new Font("Arial", BOLD, 12));
        assignButton.addActionListener(e -> {
            NFScanner selectedScanner = FingersTools.getInstance().getClient().getFingerScanner();
            if (selectedScanner != null) {
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        roomClient.assignDeviceToRoom(new AssignDeviceToRoomRequest(room.roomId(), selectedScanner.getId().trim()));
                        System.out.println("Assigning to room with device hardware id: " + selectedScanner.getId());
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();

                            BuildingDTO updatedBuilding = buildingClient.getBuildingById(building.id());
                            int buildingIndex = buildings.indexOf(building);
                            buildings.set(buildingIndex, updatedBuilding);

                            JOptionPane.showMessageDialog(
                                    deviceDialog,
                                    "Scanner successfully assigned to room " + room.roomNumber(),
                                    "Success",
                                    INFORMATION_MESSAGE
                            );

                            scannerLabel.setText("Scanner: " + selectedScanner.getId());
                            deviceDialog.pack();
                            deviceDialog.dispose();
                            if (parentDialog != null) {
                                parentDialog.dispose();
                            }

                            showBuildingDetails(buildingIndex);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Error while assigning device: " + ex.getMessage(),
                                    "Error",
                                    ERROR_MESSAGE
                            );
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
            } else {
                JOptionPane.showMessageDialog(
                        deviceDialog,
                        "Please select a scanner from the list",
                        "No Scanner Selected",
                        WARNING_MESSAGE
                );
            }
        });

        JButton closeButton = new JButton("Cancel");
        closeButton.setCursor(getPredefinedCursor(HAND_CURSOR));

        buttonPanel.add(assignButton);
        buttonPanel.add(closeButton);
        closeButton.addActionListener(e -> deviceDialog.dispose());
        mainPanel.add(buttonPanel, SOUTH);

        deviceDialog.add(mainPanel);
        deviceDialog.setVisible(true);
    }

    private void updateRoom(BuildingDTO building, int roomIndex, JDialog parentDialog) {
        RoomDTO room = building.rooms().get(roomIndex);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Enter new room number:"));
        JTextField roomNumberField = new JTextField(room.roomNumber());
        panel.add(roomNumberField);

        panel.add(new JLabel("Enter new floor number:"));
        JTextField floorField = new JTextField(String.valueOf(room.floor()));
        panel.add(floorField);

        int result = JOptionPane.showConfirmDialog(
                parentDialog,
                panel,
                "Update Room",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newRoomNumber = roomNumberField.getText().trim();
            String newFloorText = floorField.getText().trim();

            if (newRoomNumber.isEmpty() || newFloorText.isEmpty()) {
                JOptionPane.showMessageDialog(parentDialog, "All fields are required.", "Input Error", ERROR_MESSAGE);
                return;
            }

            try {
                int newFloor = Integer.parseInt(newFloorText);

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        roomClient.updateRoomWithId(room.roomId(), new UpdateRoomRequest(newRoomNumber, newFloor));
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();

                            updateBuildingTable();
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Rom has been updated!!",
                                    "Success",
                                    INFORMATION_MESSAGE
                            );
                        } catch (InterruptedException | ExecutionException e) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Failed to update room: " + e.getMessage(),
                                    "Error",
                                    ERROR_MESSAGE
                            );
                        }
                    }
                };

                worker.execute();

                List<RoomDTO> updatedRooms = new ArrayList<>(building.rooms());
                updatedRooms.set(roomIndex, new RoomDTO(room.roomId(), newRoomNumber, newFloor, room.hardwareDeviceId()));

                int buildingIndex = buildings.indexOf(building);
                buildings.set(buildingIndex, new BuildingDTO(
                        building.id(),
                        building.buildingNumber(),
                        building.street(),
                        updatedRooms
                ));

                updateBuildingTable();
                parentDialog.dispose();
                showBuildingDetails(buildingIndex);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentDialog, "Floor must be a valid number.", "Input Error", ERROR_MESSAGE);
            }
        }
    }

    private void deleteRoom(BuildingDTO building, int roomIndex, JDialog parentDialog) {
        int confirm = JOptionPane.showConfirmDialog(
                parentDialog,
                "Are you sure you want to delete this room?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {  // tODO: delete building rom db here
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    roomClient.deleteRoomWithId(building.rooms().get(roomIndex).roomId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        List<RoomDTO> updatedRooms = new ArrayList<>(building.rooms());
                        updatedRooms.remove(roomIndex);

                        int buildingIndex = buildings.indexOf(building);
                        buildings.set(buildingIndex, new BuildingDTO(
                                building.id(),
                                building.buildingNumber(),
                                building.street(),
                                updatedRooms
                        ));

                        JOptionPane.showMessageDialog(
                                null,
                                "Rom has been updated!!",
                                "Success",
                                INFORMATION_MESSAGE
                        );
                        updateBuildingTable();
                        parentDialog.dispose();
                        showBuildingDetails(buildingIndex);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Failed to update room: " + e.getMessage(),
                                "Error",
                                ERROR_MESSAGE
                        );
                        e.printStackTrace();
                    }
                }
            };

            worker.execute();
        }
    }

    private void updateBuildingTable() {
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
    }

    private void updateRoomTable() {
        roomTableModel.setRowCount(0);
        for (RoomDTO room : rooms) {
            roomTableModel.addRow(new Object[]{
                    room.roomId(),
                    room.roomNumber(),
                    room.floor(),
                    "Update",
                    "Delete",
                    "Set Device"
            });
        }
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
}
