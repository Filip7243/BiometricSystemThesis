package com.example.gui.tabs;

import com.example.client.BaseResourceWorker;
import com.example.client.BuildingService;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.UpdateBuildingRequest;

import javax.swing.*;
import java.awt.*;

import static java.awt.Color.RED;
import static java.awt.Cursor.*;
import static java.awt.Font.BOLD;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

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
        setTitle("Edit building: " + building.buildingNumber());
        setSize(400, 500);
        setLocationRelativeTo(null);

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

        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(RED);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;

        JButton saveButton = new JButton("Save");
        saveButton.setCursor(getPredefinedCursor(HAND_CURSOR));
        saveButton.addActionListener(e -> {
            saveButton.setEnabled(false);
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("Updating building...");

            UpdateBuildingRequest updateRequest = new UpdateBuildingRequest(
                    buildingNumberField.getText(),
                    streetField.getText()
            );

            buildingService.updateBuildingWithId(
                    building.id(),
                    updateRequest,
                    (result) -> {
                        callback.run();
                        dispose();
                    },
                    this
            );
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        panel.add(saveButton, gbc);

        add(panel);

        setVisible(true);
    }
}
