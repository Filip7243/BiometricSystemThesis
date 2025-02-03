package com.example.gui;

import com.example.model.Role;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;

public final class StyledComponentFactory {

    public static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color); // Green
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(color.darker(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // Hover effect
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

    public static JButton createStyledButton(String text, Color color, int width, int height) {
        JButton styledButton = createStyledButton(text, color);
        styledButton.setPreferredSize(new Dimension(width, height));

        return styledButton;
    }

    public static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    public static JTextField createStyledTextField(String text) {
        JTextField textField = new JTextField(text, 20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        textField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return textField;
    }

    public static JComboBox<Role> createStyledComboBox(Role[] values, Role selectedRole) {
        JComboBox<Role> comboBox = new JComboBox<>(values);
        comboBox.setSelectedItem(selectedRole);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return comboBox;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        passwordField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return passwordField;
    }
}
