package com.example.gui;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static javax.swing.BorderFactory.createTitledBorder;

public class PersonalDataFormPanel extends JPanel {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtPesel;
    private JComboBox<String> cmbRoles;

    public PersonalDataFormPanel(DocumentListener documentListener) {
        super();
        initGUI(documentListener);
    }

    protected void initGUI(DocumentListener documentListener) {
        setLayout(new GridBagLayout());
        setBorder(createTitledBorder("Personal Data"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblFirstName = new JLabel("First name:");
        add(lblFirstName, gbc);
        gbc.gridy++;
        txtFirstName = new JTextField(20);
        txtFirstName.getDocument().addDocumentListener(documentListener);
        add(txtFirstName, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        JLabel lblLastName = new JLabel("Last name:");
        add(lblLastName, gbc);
        gbc.gridy++;
        txtLastName = new JTextField(20);
        txtLastName.getDocument().addDocumentListener(documentListener);
        add(txtLastName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblPesel = new JLabel("PESEL:");
        add(lblPesel, gbc);
        gbc.gridy++;
        txtPesel = new JTextField(20);
        txtPesel.getDocument().addDocumentListener(documentListener);
        add(txtPesel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel lblRoles = new JLabel("Roles:");
        add(lblRoles, gbc);
        gbc.gridy++;
        cmbRoles = new JComboBox<>(new String[]{"Admin", "User"});
        cmbRoles.setPreferredSize(new Dimension(150, 20));
        add(cmbRoles, gbc);
    }

    String getFirstName() {
        return txtFirstName.getText().trim();
    }

    String getLastName() {
        return txtLastName.getText().trim();
    }

    String getPesel() {
        return txtPesel.getText().trim();
    }

    JComboBox<String> getCmbRoles() {
        return cmbRoles;
    }
}
