package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static java.awt.BorderLayout.*;

public class MenageDevicesTab extends BasePanel implements ActionListener {

    private JButton btnSaveAddDevice;
    private ScannersListPanel scannersListPanel;
    private BuildingRoomsPanel buildingRoomsPanel;
    private RemoveScannerFromRoomPanel removeScannerFromRoomPanel;

    public MenageDevicesTab() {
        super();

        requiredLicenses = new ArrayList<>();
        requiredLicenses.add("Devices.FingerScanners");

        optionalLicenses = new ArrayList<>();
    }

    BuildingRoomsPanel getBuildingRoomsPanel() {
        return buildingRoomsPanel;
    }

    ScannersListPanel getScannersListPanel() {
        return scannersListPanel;
    }

    @Override
    protected void initGUI() {
        setLayout(new BorderLayout());

        panelLicensing = new LicensingPanel(requiredLicenses, optionalLicenses);
        add(panelLicensing, NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());

        scannersListPanel = new ScannersListPanel();
        northPanel.add(scannersListPanel, NORTH);

        buildingRoomsPanel = new BuildingRoomsPanel();
        buildingRoomsPanel.setListRoomsSingleSelectionModel();
        buildingRoomsPanel.removeDefaultListeners();
        buildingRoomsPanel.hideButtons();
        buildingRoomsPanel.getBtnAddRooms().addActionListener(this);
        buildingRoomsPanel.getBtnRefreshLists().addActionListener(this);
        northPanel.add(buildingRoomsPanel, CENTER);

        removeScannerFromRoomPanel = new RemoveScannerFromRoomPanel();
        northPanel.add(removeScannerFromRoomPanel, SOUTH);

        btnSaveAddDevice = new JButton("Save");
        btnSaveAddDevice.addActionListener(this);
        btnSaveAddDevice.setPreferredSize(new Dimension(btnSaveAddDevice.getPreferredSize().width, 40));
        mainPanel.add(btnSaveAddDevice, SOUTH);

        mainPanel.add(northPanel, NORTH);
    }

    @Override
    protected void setDefaultValues() {

    }

    @Override
    protected void updateControls() {

    }

    @Override
    protected void updateFingersTools() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
