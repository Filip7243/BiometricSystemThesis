package com.example.gui.tabs;

import com.example.client.UserService;
import com.example.client.dto.UpdateUserRequest;
import com.example.client.dto.UserDTO;
import com.example.model.Role;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

import static java.awt.Cursor.*;
import static java.awt.FlowLayout.RIGHT;
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
        setSize(400, 300);
        setLocationRelativeTo(null);

        JTextField firstNameField = new JTextField(user.firstName(), 20);
        firstNameField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        JTextField lastNameField = new JTextField(user.lastName(), 20);
        lastNameField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        JTextField peselField = new JTextField(user.pesel(), 20);
        peselField.setCursor(getPredefinedCursor(TEXT_CURSOR));

        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        roleCombo.setSelectedItem(user.role());
        roleCombo.setCursor(getPredefinedCursor(HAND_CURSOR));

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("PESEL:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(peselField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(roleCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(e -> {
            UpdateUserRequest request = new UpdateUserRequest(
                    user.id(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    peselField.getText().trim(),
                    ((Role) Objects.requireNonNull(roleCombo.getSelectedItem())).name()
            );

            userService.updateUser(
                    request,
                    (result) -> {
                        onSuccessCallback.accept(null);
                        JOptionPane.showMessageDialog(
                                this,
                                "User updated successfully",
                                "Success",
                                INFORMATION_MESSAGE
                        );
                        dispose();
                    },
                    this
            );
        });
        cancelButton.addActionListener(e -> dispose());

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }
}
