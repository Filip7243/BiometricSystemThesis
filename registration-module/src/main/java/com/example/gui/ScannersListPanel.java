package com.example.gui;

import com.example.FingersTools;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NFingerScanner;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

import static com.example.gui.StyledComponentFactory.createStyledButton;
import static com.neurotec.devices.NDeviceType.FINGER_SCANNER;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;

public class ScannersListPanel extends JPanel implements ActionListener {

    private final NDeviceManager deviceManager;

    private JList<NDevice> listScanners;
    private JButton btnRefresh;

    public ScannersListPanel() {
        super();

        FingersTools.getInstance().getClient().setUseDeviceManager(true);
        deviceManager = FingersTools.getInstance().getClient().getDeviceManager();
        deviceManager.setDeviceTypes(EnumSet.of(FINGER_SCANNER));
        deviceManager.initialize();

        initGUI();
    }

    protected void initGUI() {
        setPreferredSize(new Dimension(getPreferredSize().width, 150));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                "Scanners",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
        ));

        // Modern styling for JList
        listScanners = new JList<>();
        listScanners.setModel(new DefaultListModel<>());
        listScanners.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScanners.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));  // Lighter border
        listScanners.setBackground(new Color(245, 245, 245));  // Light gray background
        listScanners.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listScanners.setSelectionBackground(new Color(52, 152, 219));  // Blue background when selected
        listScanners.setSelectionForeground(Color.WHITE);  // White text when selected
        listScanners.setFocusable(false);  // To prevent focus outline when clicked
        listScanners.addListSelectionListener(new ScannerSelectionListener());

        // Styled Refresh Button
        btnRefresh = createStyledButton("Refresh", new Color(23, 162, 184), 150, 15);
        btnRefresh.addActionListener(this);

        // GridBagConstraints for layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add JList with scroll
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane scrollPaneList = new JScrollPane(listScanners);
        scrollPaneList.setPreferredSize(new Dimension(scrollPaneList.getPreferredSize().width, 150)); // Increase height for better appearance
        add(scrollPaneList, gbc);

        // Add Refresh Button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;  // Now it spans the full width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;  // Makes the button stretch horizontally
        gbc.weighty = 0;    // No vertical stretch
        add(btnRefresh, gbc);
    }

    public void updateScannerList() {
        DefaultListModel<NDevice> model = (DefaultListModel<NDevice>) listScanners.getModel();
        model.clear();

        for (NDevice device : deviceManager.getDevices()) {
            model.addElement(device);
        }

        NFingerScanner scanner = (NFingerScanner) FingersTools.getInstance().getClient().getFingerScanner();
        if ((scanner == null) && (model.getSize() > 0)) {
            listScanners.setSelectedIndex(0);
        } else if (scanner != null) {
            listScanners.setSelectedValue(scanner, true);
        }
    }

    private NFingerScanner getSelectedScanner() {
        return (NFingerScanner) listScanners.getSelectedValue();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            updateScannerList();
        }
    }

    private class ScannerSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            FingersTools.getInstance().getClient().setFingerScanner(getSelectedScanner());
        }
    }
}
