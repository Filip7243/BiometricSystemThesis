package com.example.gui;

import com.neurotec.util.concurrent.AggregateExecutionException;

import javax.swing.*;
import java.util.List;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

public abstract class BasePanel extends JPanel {

    protected LicensingPanel panelLicensing;
    protected List<String> requiredLicenses;
    protected List<String> optionalLicenses;
    protected boolean obtained;

    protected abstract void initGUI();

    protected abstract void setDefaultValues();

    protected abstract void updateControls();

    protected abstract void updateFingersTools();

    public void init() {
        initGUI();
        setDefaultValues();
        updateControls();
    }

    public final void updateLicensing(boolean status) {
        panelLicensing.setComponentObtainingStatus(status);
        obtained = status;
    }

    public final LicensingPanel getLicensingPanel() {
        return panelLicensing;
    }

    public void showError(Throwable e) {
        if (e instanceof AggregateExecutionException) {
            StringBuilder sb = new StringBuilder(64);
            sb.append("Execution resulted in one or more errors:\n");
            for (Throwable cause : ((AggregateExecutionException) e).getCauses()) {
                sb.append(cause.toString()).append('\n');
            }
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, sb.toString(),
                    "Execution failed", ERROR_MESSAGE));
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, e, "Error", ERROR_MESSAGE);
            });
        }
    }

    public void showError(String message) {
        if (message == null) throw new NullPointerException("Message is null");
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message));
    }

    public LicensingPanel getPanelLicensing() {
        return panelLicensing;
    }

    public List<String> getRequiredLicenses() {
        return requiredLicenses;
    }

    public List<String> getOptionalLicenses() {
        return optionalLicenses;
    }

    public boolean isObtained() {
        return obtained;
    }
}
