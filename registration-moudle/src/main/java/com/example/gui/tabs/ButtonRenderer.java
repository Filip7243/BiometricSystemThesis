package com.example.gui.tabs;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static java.awt.Font.BOLD;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer(String function) {
        setOpaque(true);

        switch (function) {
            case "Edit":
                setBackground(Color.LIGHT_GRAY);
                setFont(new Font("Arial", BOLD, 12));
                break;
            case "Delete":
                setBackground(new Color(255, 51, 0));
                setFont(new Font("Arial", BOLD, 12));
                break;
            case "Details":
                setBackground(Color.LIGHT_GRAY);
                setFont(new Font("Arial", BOLD, 12));
                break;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}
