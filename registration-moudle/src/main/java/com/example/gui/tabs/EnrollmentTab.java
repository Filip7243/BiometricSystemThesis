package com.example.gui.tabs;

import com.example.client.*;
import com.example.client.dto.*;
import com.example.gui.BasePanel;
import com.example.model.FingerType;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.time.ZoneId.systemDefault;

// TODO: styling add confirm buttons and done, search bar in users tabel and buildings table, sort by id, login panel, polaczyc zamek

public class EnrollmentTab extends BasePanel implements ActionListener {

    private JTabbedPane chartTabbedPane;
    private EnrollmentService enrollmentService;
    private BuildingService buildingService;
    private UserService userService;
    private RoomService roomService;

    private JComboBox<BuildingDTO> buildingSelector;
    private JComboBox<UserDTO> hourlyEnrollmentsUserSelector;
    private JComboBox<UserDTO> lateControlUserSelector;
    private JComboBox<RoomDTO> roomSelector;
    private JSpinner hourSpinner;
    private ModerDatePicker roomDatePicker;
    private ModerDatePicker lateControlDatePicker;

    private ChartPanel roomEntrancesChartPanel;
    private ChartPanel unconfirmedEntrancesChartPanel;
    private ChartPanel enrollmentConfirmationChartPanel;
    private ChartPanel lateControlChartPanel;

    public EnrollmentTab() {
        super();

        RoomClient roomClient = new RoomClient();

        this.enrollmentService = new EnrollmentService(new EnrollmentClient());
        this.buildingService = new BuildingService(new BuildingClient(), roomClient);
        this.userService = new UserService(new UserClient());
        this.roomService = new RoomService(roomClient);
    }

    @Override
    protected void initGUI() {
        // Use Segeo UI font for all components
        Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);
        setFont(modernFont);

        // Initialize components
        buildingSelector = new JComboBox<>();
        buildingSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        buildingSelector.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        buildingSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        hourlyEnrollmentsUserSelector = new JComboBox<>();
        hourlyEnrollmentsUserSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hourlyEnrollmentsUserSelector.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        hourlyEnrollmentsUserSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lateControlUserSelector = new JComboBox<>();
        lateControlUserSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lateControlUserSelector.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        lateControlUserSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        roomSelector = new JComboBox<>();
        roomSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomSelector.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        roomSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        hourSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));

        loadBuildings();
        loadUsers();
        loadRooms();

        buildingSelector.addActionListener(e -> updateRoomEntrancesChart());
        hourlyEnrollmentsUserSelector.addActionListener(e -> {
            updateEnrollmentConfirmationChart();
        });
        lateControlUserSelector.addActionListener(e -> {
            updateLateControlChart();
        });

        // Set Layout with BorderLayout and add some padding
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and add the tabbed pane
        chartTabbedPane = createChartTabbedPane();
        add(chartTabbedPane, BorderLayout.CENTER);
    }

    private JTabbedPane createChartTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Room Entrances Chart Tab
        JPanel roomEntrancesPanel = new JPanel(new BorderLayout(10, 10));

        // Styled header panel
        JPanel roomHeaderPanel = createStyledHeaderPanel(
                "Room Entrance Distribution",
                "Analyze room entry patterns and track building access"
        );
        roomEntrancesPanel.add(roomHeaderPanel, BorderLayout.NORTH);

        // Vertical, centered input container
        JPanel roomInputsContainer = new JPanel();
        roomInputsContainer.setLayout(new BoxLayout(roomInputsContainer, BoxLayout.Y_AXIS));
        roomInputsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Centered input panel
        JPanel centeredInputPanel = new JPanel();
        centeredInputPanel.setLayout(new BoxLayout(centeredInputPanel, BoxLayout.Y_AXIS));
        centeredInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create labeled inputs
        roomDatePicker = new ModerDatePicker();
        JPanel buildingInputPanel = createLabeledInput("Building:", buildingSelector);
        JPanel dateInputPanel = createLabeledInput("Date:", roomDatePicker);

        centeredInputPanel.add(buildingInputPanel);
        centeredInputPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Vertical spacing
        centeredInputPanel.add(dateInputPanel);

        roomInputsContainer.add(Box.createHorizontalGlue());
        roomInputsContainer.add(centeredInputPanel);
        roomInputsContainer.add(Box.createHorizontalGlue());

        roomEntrancesChartPanel = new ChartPanel(null);
        roomEntrancesChartPanel.setPreferredSize(new Dimension(500, 350));

        roomEntrancesPanel.add(roomInputsContainer, BorderLayout.CENTER);
        roomEntrancesPanel.add(roomEntrancesChartPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Room Entrance Distribution", roomEntrancesPanel);

        // Unconfirmed Entrances Chart Tab
        JPanel unconfirmedPanel = new JPanel(new BorderLayout(10, 10));

        JPanel unconfirmedHeaderPanel = createStyledHeaderPanel(
                "Unconfirmed Entrances per Room",
                "Track and analyze unconfirmed room entries"
        );
        unconfirmedPanel.add(unconfirmedHeaderPanel, BorderLayout.NORTH);

        unconfirmedEntrancesChartPanel = new ChartPanel(null);
        unconfirmedEntrancesChartPanel.setPreferredSize(new Dimension(500, 350));

        unconfirmedPanel.add(unconfirmedEntrancesChartPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Daily Trend", unconfirmedPanel);

        // Enrollment Confirmation Chart Tab
        JPanel enrollmentPanel = new JPanel(new BorderLayout(5, 5));

        JPanel enrollmentHeaderPanel = createStyledHeaderPanel(
                "User's Unconfirmed Enrollments By Finger Type",
                "Chart shows user's unconfirmed enrollments to rooms that he is assigned"
        );
        enrollmentPanel.add(enrollmentHeaderPanel, BorderLayout.NORTH);

        JPanel enrollmentInputsContainer = new JPanel(new GridBagLayout());
        enrollmentInputsContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel enrollmentCenteredPanel = new JPanel();
        enrollmentCenteredPanel.setLayout(new BoxLayout(enrollmentCenteredPanel, BoxLayout.Y_AXIS));
        enrollmentCenteredPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel userInputPanel = createLabeledInput("User:", hourlyEnrollmentsUserSelector);
        userInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        enrollmentCenteredPanel.add(userInputPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        enrollmentInputsContainer.add(enrollmentCenteredPanel, gbc);

        enrollmentConfirmationChartPanel = new ChartPanel(null);
        enrollmentConfirmationChartPanel.setPreferredSize(new Dimension(500, 450));

        enrollmentPanel.add(enrollmentInputsContainer, BorderLayout.CENTER);
        enrollmentPanel.add(enrollmentConfirmationChartPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Enrollment Confirmation", enrollmentPanel);

        // Late Control Chart Tab
        JPanel lateControlPanel = new JPanel(new BorderLayout(10, 10));

        JPanel lateControlHeaderPanel = createStyledHeaderPanel(
                "User Enrollments",
                "Monitor and analyze user late enrollments"
        );
        lateControlPanel.add(lateControlHeaderPanel, BorderLayout.NORTH);

        // Vertical, centered input container for Late Control tab
        JPanel lateControlInputsContainer = new JPanel();
        lateControlInputsContainer.setLayout(new BoxLayout(lateControlInputsContainer, BoxLayout.Y_AXIS));
        lateControlInputsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Centered input panel
        JPanel lateControlCenteredPanel = new JPanel();
        lateControlCenteredPanel.setLayout(new BoxLayout(lateControlCenteredPanel, BoxLayout.Y_AXIS));
        lateControlCenteredPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create labeled inputs
        lateControlDatePicker = new ModerDatePicker();
        JPanel userInputPanel2 = createLabeledInput("User:", lateControlUserSelector);
        JPanel dateInputPanel3 = createLabeledInput("Date:", lateControlDatePicker);
        JPanel hourInputPanel = createLabeledInput("Hour:", hourSpinner);

        lateControlCenteredPanel.add(userInputPanel2);
        lateControlCenteredPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Vertical spacing
        lateControlCenteredPanel.add(dateInputPanel3);
        lateControlCenteredPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Vertical spacing
        lateControlCenteredPanel.add(hourInputPanel);

        lateControlInputsContainer.add(Box.createHorizontalGlue());
        lateControlInputsContainer.add(lateControlCenteredPanel);
        lateControlInputsContainer.add(Box.createHorizontalGlue());

        lateControlChartPanel = new ChartPanel(null);
        lateControlChartPanel.setPreferredSize(new Dimension(500, 350));

        lateControlPanel.add(lateControlInputsContainer, BorderLayout.CENTER);
        lateControlPanel.add(lateControlChartPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Late Control", lateControlPanel);

        return tabbedPane;
    }

    private void loadBuildings() {
        buildingService.getAllBuildings(buildings -> {
            buildingSelector.removeAllItems();
            buildings.forEach(building -> buildingSelector.addItem(building));
        }, this);
    }

    private void loadUsers() {
        userService.getAllUsers(users -> {
            hourlyEnrollmentsUserSelector.removeAllItems();
            lateControlUserSelector.removeAllItems();
            users.forEach(user -> {
                hourlyEnrollmentsUserSelector.addItem(user);
                lateControlUserSelector.addItem(user);
            });
        }, this);
    }

    private void loadRooms() {
        roomService.getAllRooms(rooms -> {
            roomSelector.removeAllItems();
            rooms.forEach(room -> roomSelector.addItem(room));
        }, this);
    }

    private void updateRoomEntrancesChart() {
        BuildingDTO selectedBuilding = (BuildingDTO) buildingSelector.getSelectedItem();
        if (selectedBuilding == null) return;

        enrollmentService.getNumberOfEntrancesToEachRoomOnDate(
                roomDatePicker.getDate(),
                selectedBuilding.id(),
                (roomEntranceData) -> {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    roomEntranceData.forEach(entry -> {
                        dataset.addValue(entry.entranceCount(), "Entrances", entry.roomNumber());
                    });

                    JFreeChart chart = ChartFactory.createBarChart(
                            null,
                            "Rooms",
                            "Number of Entrances",
                            dataset,
                            PlotOrientation.VERTICAL,
                            true, true, false
                    );

                    roomEntrancesChartPanel.setChart(chart);
                    roomEntrancesChartPanel.repaint();
                },
                this
        );
    }

    private void updateUnconfirmedEntrancesChart() {
        enrollmentService.getUnconfirmedEntrancesPerUserByRoom(
                (unconfirmedEntranceData) -> {
                    DefaultPieDataset dataset = new DefaultPieDataset();

                    // Iterate through the UnconfirmedEntranceDTO list to populate the dataset
                    unconfirmedEntranceData.forEach(entry -> {
                        dataset.setValue(entry.roomNumber(), entry.count());
                    });

                    // Create the pie chart
                    JFreeChart chart = ChartFactory.createPieChart(
                            null,
                            dataset,
                            true, true, false
                    );

                    // Update the chart panel
                    unconfirmedEntrancesChartPanel.setChart(chart);
                    unconfirmedEntrancesChartPanel.repaint();
                },
                this
        );
    }


    private void updateEnrollmentConfirmationChart() {
        UserDTO selectedUser = (UserDTO) hourlyEnrollmentsUserSelector.getSelectedItem();
        if (selectedUser == null) return;

        enrollmentService.getUserEnrollmentConfirmationRate(
                selectedUser.id(),
                (userEnrollmentData) -> {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    Map<FingerType, Long> confirmedCounts = userEnrollmentData.stream()
                            .filter(UserEnrollmentConfirmationDTO::isConfirmed)
                            .collect(Collectors.groupingBy(UserEnrollmentConfirmationDTO::fingerType, Collectors.summingLong(UserEnrollmentConfirmationDTO::count)));

                    Map<FingerType, Long> unconfirmedCounts = userEnrollmentData.stream()
                            .filter(data -> !data.isConfirmed())
                            .collect(Collectors.groupingBy(UserEnrollmentConfirmationDTO::fingerType, Collectors.summingLong(UserEnrollmentConfirmationDTO::count)));

                    confirmedCounts.forEach((fingerType, count) -> {
                        dataset.addValue(count, "Confirmed", fingerType.toString());
                    });

                    unconfirmedCounts.forEach((fingerType, count) -> {
                        dataset.addValue(count, "Unconfirmed", fingerType.toString());
                    });

                    // Create the bar chart
                    JFreeChart chart = ChartFactory.createBarChart(
                            null,
                            "Finger Types",
                            "Count",
                            dataset,
                            PlotOrientation.VERTICAL,
                            true, true, false
                    );

                    // Update the chart panel
                    enrollmentConfirmationChartPanel.setChart(chart);
                    enrollmentConfirmationChartPanel.repaint();
                },
                this
        );
    }


    private void updateLateControlChart() {
        UserDTO selectedUser = (UserDTO) lateControlUserSelector.getSelectedItem();

        if (selectedUser == null) return;

        enrollmentService.getLateControlByUserAndRoom(
                lateControlDatePicker.getDate(),
                selectedUser.id(),
                (int) hourSpinner.getValue(),
                (lateControlData) -> {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    Map<String, Long> lateEntriesByRoom = lateControlData.stream()
                            .collect(Collectors.groupingBy(LateControlDTO::roomNumber, Collectors.counting()));

                    lateEntriesByRoom.forEach((roomNumber, count) -> {
                        dataset.addValue(count, "Late Entries", roomNumber);
                    });

                    JFreeChart chart = ChartFactory.createBarChart(
                            null,
                            "Rooms",
                            "Number of Late Entries",
                            dataset,
                            PlotOrientation.VERTICAL,
                            true, true, false
                    );

                    // Update the chart panel
                    lateControlChartPanel.setChart(chart);
                    lateControlChartPanel.repaint();
                },
                this
        );
    }

    @Override
    protected void setDefaultValues() {
        if (buildingSelector.getItemCount() > 0) {
            buildingSelector.setSelectedIndex(0);
        }
        if (hourlyEnrollmentsUserSelector.getItemCount() > 0) {
            hourlyEnrollmentsUserSelector.setSelectedIndex(0);
        }
        if (lateControlUserSelector.getItemCount() > 0) {
            lateControlUserSelector.setSelectedIndex(0);
        }
        if (roomSelector.getItemCount() > 0) {
            roomSelector.setSelectedIndex(0);
        }
//        roomDatePicker.setDate(LocalDate.now());
//        lateControlDatePicker.setDate(LocalDate.now());
    }

    @Override
    protected void updateControls() {
        updateRoomEntrancesChart();
        updateUnconfirmedEntrancesChart();
        updateEnrollmentConfirmationChart();
        updateLateControlChart();
    }

    @Override
    protected void updateFingersTools() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private JPanel createStyledHeaderPanel(String title, String subTitle) {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(245, 245, 245)); // Light gray
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel headerTitle = new JLabel(title, SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(new Color(52, 73, 94)); // Dark blue-gray

        JLabel headerDetails = new JLabel(subTitle, SwingConstants.CENTER);
        headerDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerDetails.setForeground(new Color(100, 100, 100)); // Subtle gray

        headerPanel.add(headerTitle);
        headerPanel.add(headerDetails);

        return headerPanel;
    }

    private Component styleComponent(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        component.setPreferredSize(new Dimension(250, 40)); // Consistent size
        return component;
    }

    private JPanel createLabeledInput(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(100, 40));

        panel.add(label, BorderLayout.WEST);
        panel.add(inputComponent, BorderLayout.CENTER);

        return panel;
    }

    private class ModerDatePicker extends JPanel {
        private final JDateChooser dateChooser;

        public ModerDatePicker() {
            super(new BorderLayout());

            JTextField dateField = new JTextField();
            dateField.setEditable(false);

            JButton datePickerButton = new JButton("Select Date");
            datePickerButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            add(dateField, BorderLayout.CENTER);
            add(datePickerButton, BorderLayout.EAST);

            styleComponent(dateField);
            styleComponent(datePickerButton);

            this.dateChooser = new JDateChooser();
            this.dateChooser.setDateFormatString("yyyy-MM-dd");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateField.setText(sdf.format(new Date()));

            datePickerButton.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        dateChooser,
                        "Select Date",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    Date selectedDate = dateChooser.getDate();
                    if (selectedDate != null) {

                        dateField.setText(sdf.format(selectedDate));
                    }
                }
            });
        }

        public LocalDate getDate() {
            return dateChooser.getDate() != null ?
                    dateChooser.getDate().toInstant().atZone(systemDefault()).toLocalDate() :
                    now();
        }
    }
}

