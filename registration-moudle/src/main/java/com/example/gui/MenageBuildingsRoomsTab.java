package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MenageBuildingsRoomsTab extends BasePanel {

    public MenageBuildingsRoomsTab() {
        super();

        requiredLicenses = new ArrayList<>();
        optionalLicenses = new ArrayList<>();
    }

    @Override
    protected void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Layout with vertical stacking

        // Add the panels to the layout
        add(new CreateNewBuildingPanel());
        add(Box.createRigidArea(new Dimension(0, 20))); // Add space between panels
        add(new RemoveBuildingRoomPanel());
        add(Box.createRigidArea(new Dimension(0, 20))); // Add space between panels
        add(new UpdateRoomsInBuildingPanel());
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
}
