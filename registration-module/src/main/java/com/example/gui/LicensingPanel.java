package com.example.gui;

import com.example.FingersTools;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.awt.BorderLayout.*;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;

public class LicensingPanel extends JPanel {

    private static final String REQUIRED_COMPONENT_LICENSES_LABEL_TEXT = "Required component licenses: ";
    private static final String COMPONENTS_OBTAINED_STATUS_TEXT = "Component licenses successfuly obtained";
    private static final String COMPONENTS_NOT_OBTAINED_STATUS_TEXT = "Component licenses not obtained";

    private static final Color COMPONENTS_OBTAINED_STATUS_TEXT_COLOR = Color.green.darker();
    private static final Color COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR = Color.red.darker();

    private static final int BORDER_WIDTH_TOP = 5;
    private static final int BORDER_WIDTH_LEFT = 5;
    private static final int BORDER_WIDTH_BOTTOM = 5;
    private static final int BORDER_WIDTH_RIGHT = 5;

    private final List<String> requiredComponents;
    private final List<String> optionalComponents;

    private JLabel lblRequiredComponentLicenses;
    private JLabel lblComponentLicensesList;
    private JLabel lblStatus;

    public LicensingPanel(List<String> required, List<String> optional) {
        super(new BorderLayout(), true);
        init();

        if (required == null) {
            requiredComponents = new ArrayList<>();
        } else {
            requiredComponents = new ArrayList<>(required);
        }
        if (optional == null) {
            optionalComponents = new ArrayList<>();
        } else {
            optionalComponents = new ArrayList<>(optional);
        }
    }

    public LicensingPanel() {
        this(null, null);
    }

    public void setRequiredComponents(List<String> components) {
        requiredComponents.clear();
        requiredComponents.addAll(components);
        updateList();
    }

    public void setOptionalComponents(List<String> components) {
        optionalComponents.clear();
        optionalComponents.addAll(components);
        updateList();
    }

    public void setComponentObtainingStatus(boolean succeeded) {
        if (succeeded) {
            lblStatus.setText(COMPONENTS_OBTAINED_STATUS_TEXT);
            lblStatus.setForeground(COMPONENTS_OBTAINED_STATUS_TEXT_COLOR);
        } else {
            lblStatus.setText(COMPONENTS_NOT_OBTAINED_STATUS_TEXT);
            lblStatus.setForeground(COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR);
        }
    }

    private void init() {
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        {
            lblRequiredComponentLicenses = new JLabel(REQUIRED_COMPONENT_LICENSES_LABEL_TEXT);
            lblRequiredComponentLicenses.setFont(new Font(lblRequiredComponentLicenses.getFont().getName(), BOLD, 11));
            lblRequiredComponentLicenses.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT,
                    BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            this.add(lblRequiredComponentLicenses, LINE_START);
        }
        {
            lblComponentLicensesList = new JLabel();
            lblComponentLicensesList.setFont(new Font(lblComponentLicensesList.getFont().getName(), PLAIN, 11));
            lblComponentLicensesList.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT,
                    BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            this.add(lblComponentLicensesList, CENTER);
        }
        {
            lblStatus = new JLabel();
            lblStatus.setFont(new Font(lblStatus.getFont().getName(), PLAIN, 11));
            lblStatus.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT,
                    BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            setComponentObtainingStatus(false);
            this.add(lblStatus, PAGE_END);
        }
    }

    private String getRequiredComponentsString() {
        StringBuilder result = new StringBuilder();
        Map<String, Boolean> licenses = FingersTools.getInstance().getLicenses();
        for (String component : requiredComponents) {
            if (licenses.get(component)) {
                result.append("<font color=green>").append(component).append("</font>, ");
            } else {
                result.append("<font color=red>").append(component).append("</font>, ");
            }
        }
        if (!result.isEmpty()) {
            result.delete(result.length() - 2, result.length());
        }
        return result.toString();
    }

    private String getOptionalComponentsString() {
        if (optionalComponents == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Map<String, Boolean> licenses = FingersTools.getInstance().getLicenses();
        for (String component : optionalComponents) {
            if (licenses.get(component)) {
                result.append("<font color=green>").append(component).append(" (optional)</font>, ");
            } else {
                result.append("<font color=red>").append(component).append(" (optional)</font>, ");
            }
            if (!result.isEmpty()) {
                result.delete(result.length() - 2, result.length());
            }
        }
        return result.toString();
    }

    private void updateList() {
        StringBuilder result = new StringBuilder("<html>").append(getRequiredComponentsString());
        if (!optionalComponents.isEmpty()) {
            result.append(", ").append(getOptionalComponentsString());
        }
        result.append("</html");
        lblComponentLicensesList.setText(result.toString());
    }
}
