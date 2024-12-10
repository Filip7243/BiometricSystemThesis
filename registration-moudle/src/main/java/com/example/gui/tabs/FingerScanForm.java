package com.example.gui.tabs;

import com.neurotec.biometrics.swing.NFingerView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.*;
import static java.awt.Color.BLACK;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.SwingConstants.CENTER;

public class FingerScanForm extends JPanel {

    private final NFingerView thumbView, indexView, middleView;
    private final JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private final JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private final JLabel infoLabel;

    private boolean isScanning = false;
    private JDialog zoomDialog = null;

    public FingerScanForm() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Fingers Scan"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = BOTH;
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
        gbc.weighty = 0.1;
        infoLabel = new JLabel("Please scan your thumb, index, and middle fingers.");
        infoLabel.setHorizontalAlignment(CENTER); // Center the label
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

    JButton getBtnCancelMiddleScan() {
        return btnCancelMiddleScan;
    }

    boolean isScanning() {
        return isScanning;
    }

    void setScanning(boolean scanning) {
        isScanning = scanning;
    }

    boolean areAllFingersScanned() {
        return thumbView.getFinger() != null && indexView.getFinger() != null && middleView.getFinger() != null;
    }

    private JPanel createFingerPanel(String title, NFingerView view, JButton scanBtn, JButton cancelBtn) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(createLineBorder(BLACK), title));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        view.setShownImage(ORIGINAL);
        view.setAutofit(true);
        scrollPane.setViewportView(view);

        view.setPreferredSize(new Dimension(300, 400));

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (view.getFinger() != null) {
                    view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                view.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && view.getFinger() != null) {
                    showZoomedView(view, title);
                }
            }
        });

        cancelBtn.setEnabled(false);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(scanBtn, WEST);
        btnPanel.add(cancelBtn, EAST);

        mainPanel.add(btnPanel, SOUTH);

        return mainPanel;
    }

    private void showZoomedView(NFingerView originalView, String title) {
        if (zoomDialog != null) {
            zoomDialog.dispose();
        }

        zoomDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title + " - Zoomed View", false);
        zoomDialog.setLayout(new BorderLayout());

        NFingerView zoomedView = new NFingerView();
        zoomedView.setFinger(originalView.getFinger());
        zoomedView.setShownImage(ORIGINAL);
        zoomedView.setAutofit(false);

        JScrollPane scrollPane = new JScrollPane(zoomedView);
        scrollPane.setPreferredSize(new Dimension(600, 800));

        zoomDialog.add(scrollPane, BorderLayout.CENTER);

        // Add a close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> zoomDialog.dispose());
        zoomDialog.add(closeButton, SOUTH);

        zoomDialog.pack();
        zoomDialog.setLocationRelativeTo(this);
        zoomDialog.setVisible(true);
    }
}
