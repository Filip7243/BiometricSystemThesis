package com.example.gui.tabs;

import com.neurotec.biometrics.swing.NFingerView;

import javax.swing.*;
import java.awt.*;

import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.Color.BLACK;
import static javax.swing.BorderFactory.createLineBorder;

public class FingerScanForm extends JPanel {

    private final NFingerView thumbView, indexView, middleView;
    private final JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private final JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private final JLabel infoLabel;

    private boolean isScanning = false;

    public FingerScanForm() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Fingers Scan"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createFingerPanel(
                "THUMB",
                thumbView = new NFingerView(),
                btnScanThumb = new JButton("SCAN THUMB"),
                btnCancelThumbScan = new JButton("CANCEL SCAN")
        ), gbc);

        gbc.gridx = 1;
        add(createFingerPanel(
                "INDEX",
                indexView = new NFingerView(),
                btnScanIndex = new JButton("SCAN INDEX"),
                btnCancelIndexScan = new JButton("CANCEL SCAN")
        ), gbc);

        gbc.gridx = 2;
        add(createFingerPanel(
                "MIDDLE",
                middleView = new NFingerView(),
                btnScanMiddle = new JButton("SCAN MIDDLE"),
                btnCancelMiddleScan = new JButton("CANCEL SCAN")
        ), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        infoLabel = new JLabel("Please scan your thumb, index, and middle fingers.");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the label
        add(infoLabel, gbc);
    }

    void updateStatus(String info) {
        infoLabel.setText(info);
    }

    void updateShownImage(NFingerView view) {
        view.setShownImage(ORIGINAL);
    }

    NFingerView getThumbView() {
        return thumbView;
    }

    NFingerView getIndexView() {
        return indexView;
    }

    NFingerView getMiddleView() {
        return middleView;
    }

    JButton getBtnScanThumb() {
        return btnScanThumb;
    }

    JButton getBtnScanIndex() {
        return btnScanIndex;
    }

    JButton getBtnScanMiddle() {
        return btnScanMiddle;
    }

    JButton getBtnCancelThumbScan() {
        return btnCancelThumbScan;
    }

    JButton getBtnCancelIndexScan() {
        return btnCancelIndexScan;
    }

    public JButton getBtnCancelMiddleScan() {
        return btnCancelMiddleScan;
    }

    boolean isScanning() {
        return isScanning;
    }

    void setScanning(boolean scanning) {
        isScanning = scanning;
    }

    private JPanel createFingerPanel(String title, NFingerView view, JButton scanBtn, JButton cancelBtn) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(createLineBorder(BLACK), title));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        view.setShownImage(ORIGINAL);
        view.setAutofit(true);
        scrollPane.setViewportView(view);

        cancelBtn.setEnabled(false);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(scanBtn, BorderLayout.WEST);
        btnPanel.add(cancelBtn, BorderLayout.EAST);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }
}
