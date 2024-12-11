package com.example.gui.tabs;

import com.example.client.BuildingService;
import com.example.client.dto.BuildingDTO;
import com.example.client.dto.CreateBuildingRequest;
import com.example.client.dto.CreateRoomRequest;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AddBuildingDialog extends JDialog {

    private final BuildingService buildingService;
    private final Consumer<BuildingDTO> refreshCallback;

    private DefaultListModel<CreateRoomRequest> roomListModel;

    public AddBuildingDialog(Frame parent,
                             BuildingService buildingService,
                             Consumer<BuildingDTO> refreshCallback) {
        super(parent, "Create new building", true);

        setLocationRelativeTo(null);

        this.buildingService = buildingService;
        this.refreshCallback = refreshCallback;

        initComponents();
    }

    private void initComponents() {
        setSize(700, 600);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new GridBagLayout());
        JLabel headerLabel = new JLabel("Create New Building");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(52, 73, 94));
        headerPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
        headerPanel.add(headerLabel);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Styled components
        JTextField buildingNumberField = createStyledTextField("");
        JTextField streetField = createStyledTextField("");

        roomListModel = new DefaultListModel<>();
        JList<CreateRoomRequest> roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        roomScrollPane.setPreferredSize(new Dimension(300, 150));

        roomList.setCellRenderer(new RoomListCellRenderer());
        roomList.setFixedCellHeight(40); // Adjust cell height
        roomList.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199))); // Light gray border

        // ScrollPane Styling
        roomScrollPane.setBorder(BorderFactory.createLineBorder(new Color(127, 140, 141))); // Gray border

        // Optional: Change ScrollBar colors
        roomScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(52, 152, 219); // Blue scrollbar thumb
                this.trackColor = new Color(236, 240, 241); // Light gray track
            }
        });

        JButton addRoomButton = createStyledButton("Add Room", new Color(52, 152, 219)); // Blue
        addRoomButton.addActionListener(e -> new AddOrUpdateRoomInBuildingDialog(
                (Frame) getParent(),
                false,
                null,
                roomListModel,
                "Add New Room",
                null)
        );

        JButton removeRoomButton = createStyledButton("Remove Room", new Color(231, 76, 60)); // Red
        removeRoomButton.addActionListener(e -> {
            int selectedIndex = roomList.getSelectedIndex();
            if (selectedIndex != -1) {
                roomListModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a room to remove.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // Labels and Fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(createStyledLabel("Building Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buildingNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(createStyledLabel("Street:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(streetField, gbc);

        // Room Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(addRoomButton, gbc);

        gbc.gridy = 3;
        mainPanel.add(removeRoomButton, gbc);

        // Room List
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(roomScrollPane, gbc);

        // Bottom Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton saveButton = createStyledButton("Save", new Color(46, 204, 113)); // Green
        JButton clearButton = createStyledButton("Clear", new Color(241, 196, 15)); // Yellow

        saveButton.addActionListener(e -> {
            List<CreateRoomRequest> rooms = new ArrayList<>();
            for (int i = 0; i < roomListModel.size(); i++) {
                rooms.add(roomListModel.get(i));
            }

            CreateBuildingRequest request = new CreateBuildingRequest(
                    buildingNumberField.getText(),
                    streetField.getText(),
                    rooms
            );

            buildingService.saveBuilding(
                    request,
                    (result) -> {
                        refreshCallback.accept(result);
                        dispose();
                    },
                    this
            );
        });

        clearButton.addActionListener(e -> {
            buildingNumberField.setText("");
            streetField.setText("");
            roomListModel.clear();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        // Add panels to dialog
        add(headerPanel, BorderLayout.NORTH); // Add header at the top
        add(mainPanel, BorderLayout.CENTER); // Add main content in the center
        add(buttonPanel, BorderLayout.SOUTH); // Add buttons at the bottom

        setVisible(true);
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

    private static class RoomListCellRenderer extends JLabel implements ListCellRenderer<CreateRoomRequest> {
        public RoomListCellRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding inside cells
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends CreateRoomRequest> list,
                CreateRoomRequest value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            // Set text
            setText(value != null ? value.toString() : ""); // Adjust to show the appropriate room property

            // Background and foreground colors
            if (isSelected) {
                setBackground(new Color(52, 152, 219)); // Blue for selected
                setForeground(Color.WHITE); // White text
            } else {
                setBackground(Color.WHITE); // Default background
                setForeground(new Color(70, 70, 70)); // Dark gray text
            }

            // Optional: Add hover effect
            if (cellHasFocus) {
                setBorder(BorderFactory.createLineBorder(new Color(46, 204, 113))); // Green border
            } else {
                setBorder(new EmptyBorder(5, 10, 5, 10)); // Reset padding
            }

            return this;
        }
    }
}
