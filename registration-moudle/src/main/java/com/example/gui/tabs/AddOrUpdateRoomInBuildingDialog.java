package com.example.gui.tabs;

import com.example.FingersTools;
import com.example.client.dto.AddRoomRequest;
import com.example.client.dto.CreateRoomRequest;
import com.example.gui.ScannersListPanel;
import com.neurotec.devices.NFScanner;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

import static java.awt.BorderLayout.*;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class AddOrUpdateRoomInBuildingDialog extends JDialog {
    private final DefaultListModel<CreateRoomRequest> roomListModel;
    private final Consumer<AddRoomRequest> updateBuildingCallback;

    private JTextField txtRoomNumber;
    private JSpinner floorSpinner;

    public AddOrUpdateRoomInBuildingDialog(Frame parent,
                                           boolean isUpdate,
                                           Consumer<AddRoomRequest> updateBuildingCallback,
                                           DefaultListModel<CreateRoomRequest> roomListModel,
                                           String title) {
        super(parent, "Add Room to Building", true);

        setTitle(title);
        setSize(650, 600);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = createInputPanel();
        ScannersListPanel scannersPanel = new ScannersListPanel();

        this.updateBuildingCallback = updateBuildingCallback;
        this.roomListModel = roomListModel;

        JButton btnSubmit = createStyledButton("Submit Registration", new Color(46, 204, 113));

        btnSubmit.addActionListener(e -> {
            if (isUpdate) {
                updateBuilding();
            } else {
                addRoomToForm();
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, NORTH);
        centerPanel.add(scannersPanel, CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnSubmit.setPreferredSize(new Dimension(200, btnSubmit.getPreferredSize().height));
        buttonPanel.add(btnSubmit);

        add(createHeader(title), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        scannersPanel.updateScannerList();

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(createStyledLabel("Room Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtRoomNumber = createStyledTextField("");
        mainPanel.add(txtRoomNumber, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(createStyledLabel("Floor:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        floorSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        floorSpinner.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        mainPanel.add(floorSpinner, gbc);

        return mainPanel;
    }

    private void addRoomToForm() {
        String roomNumber = txtRoomNumber.getText();
        int floor = (int) floorSpinner.getValue();

        if (roomNumber.isBlank() || floor < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields.",
                    "Missing Fields",
                    WARNING_MESSAGE
            );
            return;
        }

        CreateRoomRequest room = new CreateRoomRequest(
                roomNumber,
                floor,
                FingersTools.getInstance().getClient().getFingerScanner() != null ?
                        FingersTools.getInstance().getClient().getFingerScanner().getId() : null
        );

        System.out.println(room);

        roomListModel.addElement(room);

        dispose();
    }

    private void updateBuilding() {
        String roomNumber = txtRoomNumber.getText().trim();
        int floor = (int) floorSpinner.getValue();

        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Room number cannot be empty",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        NFScanner selectedScanner = FingersTools
                .getInstance()
                .getClient()
                .getFingerScanner();

        String hardwareDeviceId = selectedScanner != null ? selectedScanner.getId() : null;

        if (updateBuildingCallback != null) {
            updateBuildingCallback.accept(new AddRoomRequest(roomNumber, floor, 1L, hardwareDeviceId));

            dispose();
        }
    }

    private JPanel createHeader(String title) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(70, 70, 70));

        headerPanel.add(headerLabel, BorderLayout.CENTER);
        return headerPanel;
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

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color); // Green
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(color.darker(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker()); // Darker green
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color); // Default green
            }
        });

        return button;
    }
}
