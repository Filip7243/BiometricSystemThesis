package com.example.gui.tabs;

import com.example.client.UserService;
import com.example.client.dto.UpdateUserRequest;
import com.example.client.dto.UserDTO;
import com.example.model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

import static com.example.gui.StyledComponentFactory.*;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

public class EditUserDialog extends JDialog {

    private final UserDTO user;
    private final UserService userService;
    private final Consumer<Void> onSuccessCallback;

    public EditUserDialog(Frame parent, UserDTO user, UserService userService, Consumer<Void> onSuccessCallback) {
        super(parent, "Edit user", true);

        this.user = user;
        this.userService = userService;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
    }

    private void initComponents() {
        setSize(600, 500);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel("Editing User Details", SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray

        JLabel headerDetails = new JLabel(
                "User: " + user.firstName() + " " + user.lastName() + " | PESEL: " + user.pesel(),
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
        JTextField firstNameField = createStyledTextField(user.firstName());
        JTextField lastNameField = createStyledTextField(user.lastName());
        JTextField peselField = createStyledTextField(user.pesel());
        JComboBox<Role> roleCombo = createStyledComboBox(Role.values(), Role.valueOf(user.role()));

        // Labels and text fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        contentPanel.add(createStyledLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        contentPanel.add(createStyledLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        contentPanel.add(createStyledLabel("PESEL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(peselField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        contentPanel.add(createStyledLabel("Role:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        contentPanel.add(roleCombo, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = createStyledButton("Save Changes", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("Cancel", new Color(46, 204, 113));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(e -> {
            saveButton.setEnabled(false);

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String pesel = peselField.getText().trim();

            if (firstName.isBlank() || lastName.isBlank() || pesel.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "First Name, Last Name and PESEL cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                saveButton.setEnabled(true);
                return;
            }

            UpdateUserRequest request = new UpdateUserRequest(
                    user.id(),
                    firstName,
                    lastName,
                    pesel,
                    ((Role) Objects.requireNonNull(roleCombo.getSelectedItem())).name()
            );

            userService.updateUser(
                    request,
                    (result) -> {
                        SwingUtilities.invokeLater(() -> {
                            onSuccessCallback.accept(null);
                            JOptionPane.showMessageDialog(
                                    this,
                                    "User updated successfully",
                                    "Success",
                                    INFORMATION_MESSAGE
                            );
                            dispose();
                        });
                    },
                    this
            );
        });

        cancelButton.addActionListener(e -> dispose());

        // Layout components
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add some final touches
        getRootPane().setDefaultButton(saveButton);
        setResizable(false);
        setVisible(true);
    }
}
