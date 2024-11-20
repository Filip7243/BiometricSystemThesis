package com.example.gui.tabs;

import com.example.model.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class UserInputForm extends JPanel {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField peselField;
    private JComboBox<Role> roleCombo;

    public UserInputForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("User Information"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        initComponents();
    }

    private void initComponents() {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        addFormField(fieldsPanel, "First Name:", firstNameField = new JTextField(20), gbc, 0);
        addTextKeyListener(firstNameField);

        addFormField(fieldsPanel, "Last Name:", lastNameField = new JTextField(20), gbc, 1);
        addTextKeyListener(lastNameField);

        addFormField(fieldsPanel, "PESEL:", peselField = new JTextField(20), gbc, 2);
        addNumberKeyListener(peselField);

        roleCombo = new JComboBox<>(Role.values());
        addFormField(fieldsPanel, "Role:", roleCombo, gbc, 3);

        add(fieldsPanel, BorderLayout.CENTER);
    }

    // Allow only numbers in filed
    private void addNumberKeyListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                char key = ke.getKeyChar();
                peselField.setEditable(
                        key >= '0' && key <= '9' ||
                                key == KeyEvent.VK_BACK_SPACE ||
                                key == KeyEvent.VK_DELETE ||
                                key == KeyEvent.VK_LEFT ||
                                key == KeyEvent.VK_RIGHT
                );
            }
        });
    }

    // Allow only letters in fields
    private void addTextKeyListener(JTextField firstNameField) {
        firstNameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                char key = ke.getKeyChar();
                firstNameField.setEditable(
                        Character.isLetter(key) ||
                                key == KeyEvent.VK_BACK_SPACE ||
                                key == KeyEvent.VK_DELETE ||
                                key == KeyEvent.VK_LEFT ||
                                key == KeyEvent.VK_RIGHT ||
                                key == KeyEvent.VK_SPACE  // Allow spaces for compound names
                );
            }
        });
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 25));
        }
        panel.add(field, gbc);
    }

    private boolean validateFields() {
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name cannot be empty");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name cannot be empty");
            return false;
        }
        if (!validatePesel(peselField.getText().trim())) {
            showError("Invalid PESEL format");
            return false;
        }
        return true;
    }

    private boolean validatePesel(String pesel) {
        return pesel.matches("\\d{11}");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        peselField.setText("");
        roleCombo.setSelectedIndex(0);
    }

    public String getFirstName() {
        return firstNameField.getText().trim();
    }

    public String getLastName() {
        return lastNameField.getText().trim();
    }

    public String getPesel() {
        return peselField.getText().trim();
    }

    public String getRole() {
        return Objects.requireNonNull(roleCombo.getSelectedItem()).toString();
    }
}
