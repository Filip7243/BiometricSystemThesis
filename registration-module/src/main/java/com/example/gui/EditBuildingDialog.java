package com.example.gui;

import com.example.client.BuildingService;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.UpdateBuildingRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.example.gui.StyledComponentFactory.*;

public class EditBuildingDialog extends JDialog {

    private final BuildingDTO building;
    private final BuildingService buildingService;
    private final Runnable callback;

    public EditBuildingDialog(Frame owner, BuildingDTO building,
                              BuildingService buildingService, Runnable callback) {
        super(owner, true);

        this.building = building;
        this.buildingService = buildingService;
        this.callback = callback;

        initComponents();
    }

    private void initComponents() {
        setTitle("Edit Building Details");
        setSize(600, 450);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Editing Building Details", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray

        JLabel headerDetails = new JLabel(
                "Building Number: " + building.buildingNumber() + " | Street: " + building.street(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray

        headerPanel.add(headerTitle);
        headerPanel.add(headerDetails);

        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Custom styled text fields
        JTextField buildingNumberField = createStyledTextField(building.buildingNumber());
        JTextField streetField = createStyledTextField(building.street());

        // Labels
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblBuildingNumber = createStyledLabel("Building Number:");
        contentPanel.add(lblBuildingNumber, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblStreet = createStyledLabel("Street:");
        contentPanel.add(lblStreet, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(streetField, gbc);

        // Status label
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        // Save button with green styling
        JButton saveButton = createStyledButton("Save Changes", new Color(46, 204, 113));
        saveButton.addActionListener(e -> {
            String buildingNumber = buildingNumberField.getText().trim();
            String street = streetField.getText().trim();

            if (buildingNumber.isEmpty() || street.isEmpty()) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Building Number and Street cannot be empty.");
                return;
            }

            saveButton.setEnabled(false);
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("Updating building...");

            UpdateBuildingRequest updateRequest = new UpdateBuildingRequest(
                    buildingNumber,
                    street
            );

            buildingService.updateBuildingWithId(
                    building.id(),
                    updateRequest,
                    (result) -> {
                        SwingUtilities.invokeLater(() -> {
                            callback.run();
                            dispose();
                        });
                    },
                    this
            );
        });

        // Layout components
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        contentPanel.add(saveButton, gbc);

        gbc.gridy = 3;
        contentPanel.add(statusLabel, gbc);

        add(mainPanel);

        // Add some final touches
        getRootPane().setDefaultButton(saveButton);
        setResizable(false);
        setVisible(true);
    }
}
