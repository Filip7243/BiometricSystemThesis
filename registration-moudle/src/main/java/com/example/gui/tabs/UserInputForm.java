package com.example.gui.tabs;

import com.example.model.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

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
        boolean firstNmae = firstNameField.getText().trim().isEmpty();
        boolean lastName = lastNameField.getText().trim().isEmpty();
        boolean role = roleCombo.getSelectedItem() != null;

        System.out.println("pesel: " + pesel);
        System.out.println("first name: " + firstNmae);
        System.out.println("last name: " + lastName);
        System.out.println("role: " + role);

        return pesel && !firstNmae &&
                !lastName && role;
    }

    private void initComponents() {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();

        addFormField(fieldsPanel, "First Name:", firstNameField = new JTextField(20), gbc, 0, 0);
        addTextKeyListener(firstNameField);

        addFormField(fieldsPanel, "Last Name:", lastNameField = new JTextField(20), gbc, 0, 1);
        addTextKeyListener(lastNameField);

        addFormField(fieldsPanel, "PESEL:", peselField = new JTextField(20), gbc, 1, 0);
        addNumberKeyListener(peselField);

        roleCombo = new JComboBox<>(Role.values());
        addFormField(fieldsPanel, "Role:", roleCombo, gbc, 1, 1);

        add(fieldsPanel, BorderLayout.CENTER);
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
        gbc.anchor = WEST;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row, int col) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        gbc.fill = NONE;

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 25));
            field.putClientProperty("JTextField.placeholderText", "Enter " + labelText.toLowerCase());
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        }
        panel.add(field, gbc);
    }

    private boolean validatePesel(String pesel) {
        return pesel.matches("\\d{11}");
    }
}
