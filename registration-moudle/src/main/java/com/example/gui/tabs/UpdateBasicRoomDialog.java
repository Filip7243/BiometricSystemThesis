package com.example.gui.tabs;

import com.example.client.RoomService;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UpdateRoomRequest;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

import static java.lang.String.valueOf;
import static javax.swing.JOptionPane.*;

public class UpdateBasicRoomDialog extends JDialog {

    private final RoomDTO room;
    private final RoomService roomService;
    private final Runnable onSuccess;

    private JTextField txtRoomNumber;
    private JSpinner floorSpinner;

    public UpdateBasicRoomDialog(Frame owner, RoomDTO room, RoomService roomService, Runnable onSuccess) {
        super(owner, true);

        this.room = room;
        this.roomService = roomService;
        this.onSuccess = onSuccess;

        initComponents();
    }

    private void initComponents() {
        setTitle("Update Room: " + room.roomNumber());
        setSize(650, 400); // Adjust the size to fit better
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = createHeader("Update Room Details");
        JPanel inputPanel = createInputPanel();
        JButton btnSubmit = createStyledButton("Update Room", new Color(46, 204, 113));

        btnSubmit.addActionListener(e -> updateRoom());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        buttonPanel.add(btnSubmit);

        add(headerPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room number field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createStyledLabel("Room Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtRoomNumber = createStyledTextField(room.roomNumber());
        panel.add(txtRoomNumber, gbc);

        // Floor number field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(createStyledLabel("Floor:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        floorSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        floorSpinner.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(floorSpinner, gbc);

        return panel;
    }

    private void updateRoom() {
        String newRoomNumber = txtRoomNumber.getText().trim();
        int newFloorNumber = (int) floorSpinner.getValue();

        if (newRoomNumber.isEmpty() || newFloorNumber < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields correctly.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        roomService.updateRoomWithId(room.roomId(),
                new UpdateRoomRequest(newRoomNumber, newFloorNumber),
                response -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Room updated successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    onSuccess.run();
                    dispose();
                },
                this);
    }

    private JPanel createHeader(String title) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(30, 10, 0, 10));

        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(70, 70, 70));

        JLabel headerDetails = new JLabel(
                "Room Number: " + room.roomNumber() + " | Floor: " + room.floor(),
                SwingConstants.CENTER
        );
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray

        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(headerDetails, BorderLayout.CENTER);
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
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(color.darker(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }
}