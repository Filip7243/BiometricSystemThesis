package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoveScannerFromRoomPanel extends JPanel implements ActionListener {

    private JList<String> alreadyAssignedScannersList;
    private JButton btnRemove;
    private JButton btnRefresh;

    public RemoveScannerFromRoomPanel() {
        initGUI();
    }

    protected void initGUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Removing Scanner From Room"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        alreadyAssignedScannersList = new JList<>(new String[]{"Scanner 1", "Scanner 2", "Scanner 3"});
        alreadyAssignedScannersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(alreadyAssignedScannersList);
        listScrollPane.setPreferredSize(new Dimension(200, 150));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(listScrollPane, gbc);

        btnRemove = new JButton("Remove");
        btnRemove.addActionListener(this);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(btnRemove);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnRefresh);
        buttonPanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.VERTICAL;
        add(buttonPanel, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRemove) {
            JOptionPane.showMessageDialog(this, "Remove button clicked!");
        } else if (e.getSource() == btnRefresh) {
            JOptionPane.showMessageDialog(this, "Refresh button clicked!");
        }
    }

}
