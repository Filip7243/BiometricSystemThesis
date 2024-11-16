package com.example.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT;

public class MainPanel extends JPanel implements ChangeListener {

    private JTabbedPane tabbedPane;
    private RegisterUserTab registerUserTab;

    public MainPanel() {
        super(new GridLayout(1, 1));
        initGUI();
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


        add(tabbedPane);
        setPreferredSize(new Dimension(680, 600));
    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }
}
