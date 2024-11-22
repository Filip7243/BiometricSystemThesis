package com.example.gui;

import com.example.FingersTools;
import com.example.gui.tabs.AddUserTab;
import com.example.gui.tabs.ManageBuildingsRoomsTab;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT;

public final class MainPanel extends JPanel implements ChangeListener {

    private JTabbedPane tabbedPane;
    private RegisterUserTab registerUserTab;
    private MenageDevicesTab menageDevicesTab;
    private MenageBuildingsRoomsTab menageBuildingsRoomsTab;
    private AddUserTab addUserTab;
    private ManageBuildingsRoomsTab manageBuildingsRoomsTab;

    public MainPanel() {
        super(new GridLayout(1, 1));
        initGUI();
    }

    public void obtainLicenses(BasePanel panel) throws IOException {
        if (!panel.isObtained()) {
            boolean status = FingersTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
            FingersTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
            panel.getLicensingPanel().setRequiredComponents(panel.getRequiredLicenses());
            panel.getLicensingPanel().setOptionalComponents(panel.getOptionalLicenses());
            panel.updateLicensing(status);
        }
    }

    private void initGUI() {
        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.addChangeListener(this);
        this.tabbedPane.setTabLayoutPolicy(SCROLL_TAB_LAYOUT);

        // Register tab
        this.registerUserTab = new RegisterUserTab();
        this.registerUserTab.init();
        this.tabbedPane.addTab("Register User", this.registerUserTab);

        // Assign device to room
        this.menageDevicesTab = new MenageDevicesTab();
        this.menageDevicesTab.init();
        this.tabbedPane.addTab("Menage Devices", this.menageDevicesTab);

        // Manage buildings and rooms
        this.menageBuildingsRoomsTab = new MenageBuildingsRoomsTab();
        this.menageBuildingsRoomsTab.init();
        this.tabbedPane.addTab("Menage Buildings", this.menageBuildingsRoomsTab);

        addUserTab = new AddUserTab();
        addUserTab.init();
        this.tabbedPane.addTab("Add User", addUserTab);

        manageBuildingsRoomsTab = new ManageBuildingsRoomsTab((JFrame) (getParent()));
        manageBuildingsRoomsTab.init();
        this.tabbedPane.addTab("Manage Buildings and Rooms", manageBuildingsRoomsTab);

        add(tabbedPane);
        setPreferredSize(new Dimension(880, 780));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JTabbedPane pane) {
            try {
                if (pane.getSelectedComponent() instanceof RegisterUserTab) {
                    obtainLicenses(registerUserTab);
                    registerUserTab.updateFingersTools();
                    registerUserTab.getScannersListPanel().updateScannerList();
                    registerUserTab.getBuildingRoomsPanel().updateBuildingList();
                    registerUserTab.getBuildingRoomsPanel().updateRoomList();
                } else if (pane.getSelectedComponent() instanceof MenageDevicesTab) {
                    obtainLicenses(menageDevicesTab);
                    menageDevicesTab.updateFingersTools();
                    menageDevicesTab.getScannersListPanel().updateScannerList();
                    menageDevicesTab.getBuildingRoomsPanel().updateBuildingList();
                    menageDevicesTab.getBuildingRoomsPanel().updateRoomList();
                } else if (pane.getSelectedComponent() instanceof AddUserTab) {
                    obtainLicenses(addUserTab);
                    addUserTab.updateFingersTools();
                    addUserTab.getScannersListPanel().updateScannerList();
//                    uif.getBuildingRoomsPanel().updateBuildingList();
//                    uif.getBuildingRoomsPanel().updateRoomList();
                }
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Could not obtain licenses for components: " + ex,
                            "Error",
                            ERROR_MESSAGE
                    );
                });
            }
        }
    }
}
