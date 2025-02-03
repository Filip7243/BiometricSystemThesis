package com.example.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ButtonRenderer extends JButton implements TableCellRenderer {

    private static final Font DEFAULT_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Color EDIT_BACKGROUND = new Color(73, 128, 203);  // Softer blue
    private static final Color DELETE_BACKGROUND = new Color(220, 53, 69);  // Bootstrap-like red
    private static final Color DETAILS_BACKGROUND = new Color(108, 117, 125);  // Muted gray

    public ButtonRenderer(String function) {
        setOpaque(true);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(DEFAULT_FONT);
        setForeground(Color.WHITE);

        switch (function) {
            case "Edit":
                setBackground(EDIT_BACKGROUND);
                break;
            case "Delete":
                setBackground(DELETE_BACKGROUND);
                break;
            case "Details":
                setBackground(DETAILS_BACKGROUND);
                break;
            default:
                setBackground(Color.GRAY);
        }

        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton button = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                        0, 0, button.getWidth(), button.getHeight(), 10, 10
                );

                g2.setColor(button.getBackground());
                g2.fill(roundedRectangle);

                super.paint(g2, c);
                g2.dispose();
            }
        });

        // Hover and press effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(getBrighterShade(getBackground()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                switch (function) {
                    case "Edit":
                        setBackground(EDIT_BACKGROUND);
                        break;
                    case "Delete":
                        setBackground(DELETE_BACKGROUND);
                        break;
                    case "Details":
                        setBackground(DETAILS_BACKGROUND);
                        break;
                    default:
                        setBackground(Color.GRAY);
                }
            }
        });
    }

    // Helper method to create a brighter shade for hover effect
    private Color getBrighterShade(Color color) {
        return color.brighter().brighter();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());

        return this;
    }
}
