package com.example.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;

public class BuildingRoomsPanel extends JPanel implements ActionListener, ListSelectionListener {

    private JList<String> listBuildings;
    private JList<String> listRooms;

    private JButton btnAddRooms;
    private JButton btnRemoveRooms;
    private JButton btnRefreshLists;

    private Map<String, String[]> buildingToRoomsMap;
    private Set<String> selectedRoomsSet;

    public BuildingRoomsPanel() {
        super();
        initData();
        initGUI();
    }

    private void initData() {
        buildingToRoomsMap = new HashMap<>();
        buildingToRoomsMap.put("Building 1", new String[]{"Room 101", "Room 102", "Room 103"});
        buildingToRoomsMap.put("Building 2", new String[]{"Room 201", "Room 202", "Room 203"});
        buildingToRoomsMap.put("Building 3", new String[]{"Room 301", "Room 302", "Room 303"});

        System.out.println(buildingToRoomsMap);

        selectedRoomsSet = new HashSet<>();
    }

    protected void initGUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Buildings and Rooms"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel lblBuilding = new JLabel("Building:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblBuilding, gbc);

        JLabel lblRoom = new JLabel("Room:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(lblRoom, gbc);

        JLabel lblButtons = new JLabel("Actions:");
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(lblButtons, gbc);

        listBuildings = new JList<>();
        listBuildings.setModel(new DefaultListModel<>());
        listBuildings.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
        JScrollPane buildingScrollPane = new JScrollPane(listBuildings);
        buildingScrollPane.setPreferredSize(new Dimension(150, 80));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(buildingScrollPane, gbc);

        listRooms = new JList<>(new String[]{});
        listRooms.setModel(new DefaultListModel<>());
        listRooms.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
        JScrollPane roomScrollPane = new JScrollPane(listRooms);
        roomScrollPane.setPreferredSize(new Dimension(150, 80));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(roomScrollPane, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        btnAddRooms = new JButton("Add Rooms");
        btnRemoveRooms = new JButton("Remove Rooms");
        btnRefreshLists = new JButton("Refresh Lists");

        buttonPanel.add(btnAddRooms);
        buttonPanel.add(btnRemoveRooms);
        buttonPanel.add(btnRefreshLists);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 3;
        add(buttonPanel, gbc);

        btnAddRooms.addActionListener(this);
        btnRemoveRooms.addActionListener(this);
        btnRefreshLists.addActionListener(this);

        listBuildings.addListSelectionListener(this);
    }

    void hideButtons() {
        btnRemoveRooms.setVisible(false);
    }

    void removeDefaultListeners() {
        btnAddRooms.removeActionListener(this);
        btnRefreshLists.removeActionListener(this);
    }

    // TODO: maybe one method not two!
    void updateBuildingList() {
        DefaultListModel<String> model = (DefaultListModel<String>) listBuildings.getModel();
        //        model.clear();
    }

    void updateRoomList() {
        DefaultListModel<String> model = (DefaultListModel<String>) listRooms.getModel();
        //        model.clear();
    }

    void setListRoomsSingleSelectionModel() {
        listRooms.setSelectionMode(SINGLE_INTERVAL_SELECTION);
    }

    JButton getBtnAddRooms() {
        return btnAddRooms;
    }

    JButton getBtnRemoveRooms() {
        return btnRemoveRooms;
    }

    JButton getBtnRefreshLists() {
        return btnRefreshLists;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddRooms) {
            handleAddRooms();
        } else if (e.getSource() == btnRemoveRooms) {
            handleRemoveRooms();
        } else if (e.getSource() == btnRefreshLists) {
            listBuildings.clearSelection();
            initData();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            handleBuildingSelection();
        }
    }

    private void handleBuildingSelection() {
        List<String> selectedBuildings = listBuildings.getSelectedValuesList();
        List<String> combinedRooms = new ArrayList<>();

        for (String building : selectedBuildings) {
            String[] rooms = buildingToRoomsMap.get(building);
            if (rooms != null) {
                for (String room : rooms) {
                    if (!combinedRooms.contains(room)) {
                        combinedRooms.add(room);
                    }
                }
            }
        }

        listRooms.setListData(combinedRooms.toArray(new String[0]));
    }

    private void handleAddRooms() {
        List<String> selectedRooms = listRooms.getSelectedValuesList();
        selectedRoomsSet.addAll(selectedRooms);

        JOptionPane.showMessageDialog(
                BuildingRoomsPanel.this,
                "Selected Rooms: " + selectedRoomsSet,
                "User Data",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleRemoveRooms() {
        List<String> selectedRoomsToRemove = listRooms.getSelectedValuesList();
        selectedRoomsToRemove.forEach(selectedRoomsSet::remove);

        JOptionPane.showMessageDialog(
                BuildingRoomsPanel.this,
                "Remaining Rooms: " + selectedRoomsSet,
                "Updated User Data",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
