package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.DeviceClient;
import com.example.client.DeviceService;
import com.example.client.RoomService;
import com.example.client.dto.AssignDeviceToRoomRequest;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.DeviceDTO;
import com.example.client.dto.RoomDTO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.example.gui.StyledComponentFactory.createStyledLabel;
import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class AssignDeviceToRoomDialog extends JDialog {

    private final RoomService roomService;
    private final RoomDTO room;
    private final BuildingService buildingService;
    private final BuildingDTO building;
    private final Runnable onRemove;

    private final DeviceService deviceService = new DeviceService(new DeviceClient());
    private JList<DeviceDTO> deviceList;
    private DefaultListModel<DeviceDTO> deviceListModel;

    public AssignDeviceToRoomDialog(Frame owner,
                                    RoomService roomService,
                                    RoomDTO room,
                                    BuildingService buildingService,
                                    BuildingDTO building,
                                    Runnable onRemove) {
        super(owner, true);

        this.roomService = roomService;
        this.room = room;
        this.buildingService = buildingService;
        this.building = building;
        this.onRemove = onRemove;

        initComponents();
    }

    private void initComponents() {
        setTitle("Assign device for room: " + room.roomNumber());
        setSize(600, 600);
        setLocationRelativeTo(getOwner());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerAndCurrentDevicePanel = new JPanel(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Room Device Management", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray

        JLabel headerDetails = new JLabel(
                "Room Number: " + room.roomNumber() + " | Building: " + building.buildingNumber(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray

        headerPanel.add(headerTitle, BorderLayout.NORTH);
        headerPanel.add(headerDetails, BorderLayout.CENTER);

        JPanel currentDeviceAssignedToRoomPanel = new JPanel(new GridBagLayout());
        currentDeviceAssignedToRoomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Current Device",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel scannerLabel = createStyledLabel(room.macAddress() != null ?
                "Scanner: " + room.macAddress() :
                "No scanner assigned");

        gbc.gridx = 0;
        gbc.gridy = 0;
        currentDeviceAssignedToRoomPanel.add(scannerLabel, gbc);

// Remove Button for Current Device
        if (room.macAddress() != null) {
            JButton removeButton = createStyledButton("Remove Scanner", new Color(192, 57, 43), 150, 40);
            removeButton.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to remove the scanner from this room?",
                        "Confirm Remove",
                        YES_NO_OPTION
                );

                if (confirm == YES_OPTION) {
                    roomService.removeDeviceFromRoom(
                            new AssignDeviceToRoomRequest(room.roomId(), room.macAddress(), null),
                            (result) -> {
                                buildingService.getBuildingById(
                                        building.id(),
                                        (foundBuilding) -> {
                                            JOptionPane.showMessageDialog(
                                                    this,
                                                    "Scanner removed successfully",
                                                    "Success",
                                                    INFORMATION_MESSAGE
                                            );

                                            scannerLabel.setText("No scanner assigned");
                                            removeButton.setVisible(false);

                                            if (onRemove != null) {
                                                onRemove.run();
                                            }

                                            setVisible(false);
                                            dispose();
                                        },
                                        this
                                );
                            },
                            this
                    );
                }
            });

            gbc.gridx = 1;
            gbc.weightx = 0.0;
            currentDeviceAssignedToRoomPanel.add(removeButton, gbc);
        }

        headerAndCurrentDevicePanel.add(headerPanel, BorderLayout.NORTH);
        headerAndCurrentDevicePanel.add(currentDeviceAssignedToRoomPanel, BorderLayout.CENTER);

        mainPanel.add(headerAndCurrentDevicePanel, NORTH);

        // Devices List Panel
        JPanel devicesListPanel = new JPanel(new BorderLayout());
        devicesListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Available devices",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        deviceListModel = new DefaultListModel<>();
        deviceList = new JList<>(deviceListModel);
        deviceList.setSelectionMode(SINGLE_SELECTION);
        deviceList.setCellRenderer(new DeviceListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(deviceList);
        devicesListPanel.add(scrollPane, CENTER);

        mainPanel.add(devicesListPanel, CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 5));

        JButton assignButton = createStyledButton("Assign Device", new Color(46, 204, 113), 150, 40);
        assignButton.setEnabled(room.macAddress() == null);
        assignButton.addActionListener(e -> {
            DeviceDTO selectedDevice = deviceList.getSelectedValue();

            if (selectedDevice != null) {
                roomService.assignDeviceToRoom(
                        new AssignDeviceToRoomRequest(room.roomId(), selectedDevice.macAddress(), selectedDevice.scannerSerialNumber()),
                        (result) -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Device assigned successfully to room: " + room.roomNumber(),
                                    "Success",
                                    INFORMATION_MESSAGE
                            );
                            scannerLabel.setText("Device: " + selectedDevice.macAddress());
                            pack();
                            dispose();
                        },
                        this
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a device from the list",
                        "No Device Selected",
                        WARNING_MESSAGE
                );
            }
        });

        JButton closeButton = createStyledButton("Close", new Color(192, 57, 43), 100, 40);

        buttonPanel.add(assignButton);
        buttonPanel.add(closeButton);
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(buttonPanel, SOUTH);

        add(mainPanel);

        // Fetch and populate devices
        getAllDevicesNotAssignedToRoom();

        setVisible(true);
    }

    private void getAllDevicesNotAssignedToRoom() {
        deviceService.getDevicesNotAssignedToRoom(
                (devices) -> {
                    deviceListModel.clear();
                    for (DeviceDTO device : devices) {
                        deviceListModel.addElement(device);
                    }
                },
                this
        );
    }

    // Custom cell renderer for devices
    private static class DeviceListCellRenderer extends JPanel implements ListCellRenderer<DeviceDTO> {
        private final JLabel deviceIdLabel;
        private final JLabel deviceTypeLabel;

        public DeviceListCellRenderer() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            deviceIdLabel = createStyledLabel("Device MAC: ");
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(deviceIdLabel, gbc);

            deviceTypeLabel = createStyledLabel("Device Type: ");
            gbc.gridx = 1;
            add(deviceTypeLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends DeviceDTO> list,
                DeviceDTO device,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            deviceIdLabel.setText("Device MAC: " + device.macAddress());
            deviceTypeLabel.setText("Scanner SN: " + device.scannerSerialNumber());

            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            return this;
        }
    }
}