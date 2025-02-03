package com.example.gui;

import com.example.model.Role;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.example.gui.StyledComponentFactory.*;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

public class UserInputForm extends JPanel {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField peselField;
    private JComboBox<Role> roleCombo;

    public UserInputForm() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        peselField.setText("");
        roleCombo.setSelectedIndex(0);
    }

    String getFirstName() {
        return firstNameField.getText().trim();
    }

    String getLastName() {
        return lastNameField.getText().trim();
    }

    String getPesel() {
        return peselField.getText().trim();
    }

    Role getRole() {
        return (Role) roleCombo.getSelectedItem();
    }

    boolean areAllFieldsValid() {
        boolean pesel = validatePesel(peselField.getText().trim());
        boolean firstName = firstNameField.getText().trim().isEmpty();
        boolean lastName = lastNameField.getText().trim().isEmpty();
        boolean role = roleCombo.getSelectedItem() != null;

        return pesel && !firstName && !lastName && role;
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "User Data",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));
        GridBagConstraints gbc = createGridBagConstraints();

        // Add styled text fields with custom method
        addFormField(this, "First Name:", firstNameField = createStyledTextField(""), gbc, 0, 0);
        addTextKeyListener(firstNameField);

        addFormField(this, "Last Name:", lastNameField = createStyledTextField(""), gbc, 0, 1);
        addTextKeyListener(lastNameField);

        addFormField(this, "PESEL:", peselField = createStyledTextField(""), gbc, 1, 0);
        addNumberKeyListener(peselField);

        // Add styled combo box with custom method
        roleCombo = createStyledComboBox(Role.values(), Role.values()[0]);
        addFormField(this, "Role:", roleCombo, gbc, 1, 1);
    }

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

    private void addTextKeyListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                char key = ke.getKeyChar();
                field.setEditable(
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
        gbc.anchor = WEST;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row, int col) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        gbc.fill = NONE;

        JLabel label = createStyledLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private boolean validatePesel(String pesel) {
        return pesel.matches("\\d{11}");
    }
}
