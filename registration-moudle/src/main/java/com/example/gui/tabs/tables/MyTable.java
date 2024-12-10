package com.example.gui.tabs.tables;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Font.BOLD;

public class MyTable extends JTable {

    public MyTable(DefaultTableModel model) {
        setModel(model);
        setShowGrid(true);
        setGridColor(Color.LIGHT_GRAY);
        setRowHeight(30);
        setFont(new Font("Segoe UI", BOLD, 14));

        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(110, 148, 245));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                header.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(getPredefinedCursor(HAND_CURSOR));
            }
        });
    }

    public void setColumnRenderer(int columnIndex, TableCellRenderer renderer) {
        getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }
}
