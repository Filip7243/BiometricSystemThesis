package com.example.gui;

import com.example.FingersTools;
import com.example.gui.tabs.AddUserTab;
import com.example.gui.tabs.EnrollmentTab;
import com.example.gui.tabs.ManageBuildingsRoomsTab;
import com.example.gui.tabs.ManageUsersTab;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT;

public final class MainPanel extends JPanel implements ChangeListener {

    private JTabbedPane tabbedPane;
    private AddUserTab addUserTab;
    private ManageBuildingsRoomsTab manageBuildingsRoomsTab;
    private ManageUsersTab manageUsersTab;
    private EnrollmentTab enrollmentTab;

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

        addUserTab = new AddUserTab();
        addUserTab.init();
        this.tabbedPane.addTab("Add User", addUserTab);

        manageBuildingsRoomsTab = new ManageBuildingsRoomsTab();
        manageBuildingsRoomsTab.init();
        this.tabbedPane.addTab("Manage Buildings and Rooms", manageBuildingsRoomsTab);

        manageUsersTab = new ManageUsersTab();
        manageUsersTab.init();
        this.tabbedPane.addTab("Manage Users", manageUsersTab);

        enrollmentTab = new EnrollmentTab();
        enrollmentTab.init();
        this.tabbedPane.addTab("Enrollments", enrollmentTab);

        add(tabbedPane);
        setPreferredSize(new Dimension(900, 800));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JTabbedPane pane) {
            try {
                if (pane.getSelectedComponent() instanceof AddUserTab) {
                    obtainLicenses(addUserTab);
                    addUserTab.updateFingersTools();
                    addUserTab.getScannersListPanel().updateScannerList();
                } else if (pane.getSelectedComponent() instanceof ManageBuildingsRoomsTab) {
//                    obtainLicenses(manageBuildingsRoomsTab);
                } else if (pane.getSelectedComponent() instanceof ManageUsersTab) {
//                    obtainLicenses(manageUsersTab);
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
