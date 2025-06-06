package com.example.gui;

import com.neurotec.biometrics.swing.NFingerView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.example.gui.StyledComponentFactory.createStyledLabel;
import static com.neurotec.biometrics.swing.NFingerViewBase.ShownImage.ORIGINAL;
import static java.awt.BorderLayout.SOUTH;

public class FingerScanForm extends JPanel {

    private final NFingerView thumbView, indexView, middleView;
    private final JButton btnScanThumb, btnScanIndex, btnScanMiddle;
    private final JButton btnCancelThumbScan, btnCancelIndexScan, btnCancelMiddleScan;
    private final JLabel infoLabel;

    private boolean isScanning = false;
    private JDialog zoomDialog = null;

    public FingerScanForm() {
        // Use a more flexible layout
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
                "Fingerprints",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(52, 73, 94)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Determine screen size for responsive design
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;

        // Adjust layout based on screen width
        if (screenWidth < 1200) {
            // For smaller screens, use a more compact layout
            setLayout(new BorderLayout());
            JPanel fingerPanel = new JPanel(new GridLayout(1, 3, 5, 5));

            fingerPanel.add(createFingerPanel(
                    "THUMB",
                    thumbView = new NFingerView(),
                    btnScanThumb = createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelThumbScan = createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ));

            fingerPanel.add(createFingerPanel(
                    "INDEX",
                    indexView = new NFingerView(),
                    btnScanIndex = createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelIndexScan = createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ));

            fingerPanel.add(createFingerPanel(
                    "MIDDLE",
                    middleView = new NFingerView(),
                    btnScanMiddle = createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelMiddleScan =createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ));

            add(fingerPanel, BorderLayout.CENTER);
        } else {
            // For larger screens, use original GridBagLayout
            gbc.weightx = 1;
            gbc.weighty = 1;

            gbc.gridx = 0;
            gbc.gridy = 0;
            add(createFingerPanel(
                    "THUMB",
                    thumbView = new NFingerView(),
                    btnScanThumb = createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelThumbScan = createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ), gbc);

            gbc.gridx = 1;
            add(createFingerPanel(
                    "INDEX",
                    indexView = new NFingerView(),
                    btnScanIndex =createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelIndexScan = createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ), gbc);

            gbc.gridx = 2;
            add(createFingerPanel(
                    "MIDDLE",
                    middleView = new NFingerView(),
                    btnScanMiddle = createStyledButton("SCAN THUMB", new Color(52, 152, 219), 120, 40),
                    btnCancelMiddleScan = createStyledButton("CANCEL SCAN", new Color(231, 76, 60), 120, 40)
            ), gbc);
        }

        // Info label at the bottom
        GridBagConstraints infogbc = new GridBagConstraints();
        infogbc.gridx = 0;
        infogbc.gridy = 1;
        infogbc.gridwidth = 3;
        infogbc.fill = GridBagConstraints.HORIZONTAL;

        infoLabel = createStyledLabel("Please scan your thumb, index, and middle fingers.");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add info label with appropriate constraints
        if (getLayout() instanceof GridBagLayout) {
            add(infoLabel, infogbc);
        } else {
            add(infoLabel, BorderLayout.SOUTH);
        }

        // Make views more adaptive
        Dimension preferredViewSize = calculatePreferredViewSize(screenWidth);
        thumbView.setPreferredSize(preferredViewSize);
        indexView.setPreferredSize(preferredViewSize);
        middleView.setPreferredSize(preferredViewSize);
    }

    private Dimension calculatePreferredViewSize(int screenWidth) {
        if (screenWidth < 1200) {
            // Compact size for smaller screens
            return new Dimension(200, 300);
        } else if (screenWidth < 1600) {
            // Medium size for medium screens
            return new Dimension(300, 400);
        } else {
            // Large size for large screens
            return new Dimension(400, 500);
        }
    }

    private JPanel createFingerPanel(String title, NFingerView view, JButton scanBtn, JButton cancelBtn) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        view.setShownImage(ORIGINAL);
        view.setAutofit(true);
        scrollPane.setViewportView(view);

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

        // Button panel configuration
        cancelBtn.setEnabled(false);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.add(scanBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    void updateStatus(String info) {
        infoLabel.setText(info);
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

    void clearViews() {
        thumbView.setFinger(null);
        indexView.setFinger(null);
        middleView.setFinger(null);
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
