package com.example.gui;

import com.neurotec.biometrics.swing.NFingerView;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.*;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.CENTER;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.JLabel.LEFT;

public final class FingerViewPanel extends JPanel {

    private NFingerView currentView;
    private JPanel previousFingersPanel;
    private JButton btnScan;
    private JButton btnCancel;

    private JLabel lblThumb;
    private JLabel lblPointing;
    private JLabel lblMiddle;

    private JLabel lblInfo;

    public FingerViewPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(createTitledBorder("Finger View"));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        currentView = new NFingerView();
        currentView.setShownImage(ORIGINAL);
        currentView.setAutofit(true);
        scrollPane.setViewportView(currentView);

        previousFingersPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        previousFingersPanel.setBorder(createTitledBorder("Previously Scanned Fingers"));

        lblThumb = new JLabel("Thumb");
        lblPointing = new JLabel("Pointing");
        lblMiddle = new JLabel("Middle");

        previousFingersPanel.add(lblThumb);
        previousFingersPanel.add(lblPointing);
        previousFingersPanel.add(lblMiddle);

        lblInfo = new JLabel("Quality: -", LEFT);
        lblInfo.setFont(new Font("Arial", PLAIN, 12));
        add(lblInfo, SOUTH);

        add(previousFingersPanel, NORTH);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = CENTER;

        btnScan = new JButton("Scan");
        btnCancel = new JButton("Cancel");

        buttonPanel.add(btnScan, gbc);
        gbc.gridy++;
        buttonPanel.add(btnCancel, gbc);

        add(buttonPanel, EAST);
    }

    public void addPreviousScan(BufferedImage thumbImage, BufferedImage pointingImage, BufferedImage middleImage) {
        if (thumbImage != null) {
            lblThumb.setIcon(new ImageIcon(thumbImage));
        }

        if (pointingImage != null) {
            lblPointing.setIcon(new ImageIcon(pointingImage));
        }

        if (middleImage != null) {
            lblMiddle.setIcon(new ImageIcon(middleImage));
        }

        previousFingersPanel.revalidate();
        previousFingersPanel.repaint();
    }

    NFingerView getFingerView() {
        return currentView;
    }

    JButton getBtnScan() {
        return btnScan;
    }

    JButton getBtnCancel() {
        return btnCancel;
    }

    JLabel getLblInfo() {
        return lblInfo;
    }

    void updateShownImage() {
        currentView.setShownImage(ORIGINAL);
    }
}
