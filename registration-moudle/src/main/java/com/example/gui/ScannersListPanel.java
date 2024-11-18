package com.example.gui;

import com.example.FingersTools;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NFingerScanner;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import static com.neurotec.devices.NDeviceType.FINGER_SCANNER;

public class ScannersListPanel extends JPanel implements ActionListener {

    private final NDeviceManager deviceManager;

    private JList<NDevice> listScanners;
    private JComboBox<String> cmbFingers;
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
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Scanners"));

        listScanners = new JList<>();
        listScanners.setModel(new DefaultListModel<>());
        listScanners.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScanners.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        listScanners.addListSelectionListener(new ScannerSelectionListener());

        cmbFingers = new JComboBox<>(new String[]{"THUMB", "POINTING", "MIDDLE", "RING"});

        btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JScrollPane scrollPaneList = new JScrollPane(listScanners);
        scrollPaneList.setPreferredSize(new Dimension(0, 90));
        add(scrollPaneList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(cmbFingers, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(btnRefresh, gbc);
    }

    private NFingerScanner getSelectedScanner() {
        return (NFingerScanner) listScanners.getSelectedValue();
    }

    void updateScannerList() {
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

    void hideFingersCombo() {
        cmbFingers.setVisible(false);
    }
}
