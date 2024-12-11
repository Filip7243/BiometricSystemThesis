package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.BuildingService;
import com.example.client.RoomService;
import com.example.client.dto.AssignDeviceToRoomRequest;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.RoomDTO;
import com.example.gui.ScannersListPanel;
import com.neurotec.devices.NFScanner;

import javax.swing.*;

import java.awt.*;
import java.util.List;

import static java.awt.BorderLayout.*;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.FlowLayout.RIGHT;
import static java.awt.Font.BOLD;
import static javax.swing.JOptionPane.*;

public class AssignDeviceToRoomDialog extends JDialog {

    private final RoomService roomService;
    private final RoomDTO room;
    private final ScannersListPanel scannersListPanel;
    private final BuildingService buildingService;
    private final BuildingDTO building;
    private final Runnable onRemove;

    public AssignDeviceToRoomDialog(Frame owner,
                                    RoomService roomService,
                                    RoomDTO room,
                                    BuildingService buildingService,
                                    BuildingDTO building,
                                    Runnable onRemove) {
        super(owner, true);

        this.roomService = roomService;
        this.room = room;
        this.scannersListPanel = new ScannersListPanel();
        this.buildingService = buildingService;
        this.building = building;
        this.onRemove = onRemove;

        initComponents();
    }

    private void initComponents() {
        setTitle("Assign device for room: " + room.roomNumber());
        setSize(500, 300);
        setLocationRelativeTo(getOwner());

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
                        this,
                        "Are you sure you want to remove the scanner from this room?",
                        "Confirm Remove",
                        YES_NO_OPTION
                );

                if (confirm == YES_OPTION) {
                    roomService.removeDeviceFromRoom(
                            new AssignDeviceToRoomRequest(room.roomId(), room.hardwareDeviceId()),
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
            currentScannerPanel.add(removeButton, gbc);
        }

        mainPanel.add(currentScannerPanel, NORTH);

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
            NFScanner selectedScanner = FingersTools
                    .getInstance()
                    .getClient()
                    .getFingerScanner();

            if (selectedScanner != null) {
                roomService.assignDeviceToRoom(
                        new AssignDeviceToRoomRequest(room.roomId(), selectedScanner.getId().trim()),
                        (result) -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Scanner assigned successfully to room: " + room.roomNumber(),
                                    "Success",
                                    INFORMATION_MESSAGE
                            );
                            scannerLabel.setText("Scanner: " + selectedScanner.getId().trim());
                            pack();
                            dispose();
                        },
                        this
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
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
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(buttonPanel, SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}
