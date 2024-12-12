package com.example.gui.tabs;

import com.example.client.*;
import com.example.client.dto.*;
import com.example.gui.BasePanel;
import com.example.model.FingerType;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
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
    private JComboBox<UserDTO> userSelector;
    private JComboBox<RoomDTO> roomSelector;
    private JSpinner hourSpinner;
    private JDatePickerImpl roomDatePicker;
    private JDatePickerImpl lateControlDatePicker;

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
        userSelector = new JComboBox<>();
        roomSelector = new JComboBox<>();
        hourSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));

        loadBuildings();
        loadUsers();
        loadRooms();

        buildingSelector.addActionListener(e -> updateRoomEntrancesChart());
        userSelector.addActionListener(e -> {
            updateUnconfirmedEntrancesChart();
            updateEnrollmentConfirmationChart();
            updateLateControlChart();
        });

        // Set Layout with GroupLayout for better control
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and add the tabbed pane
        chartTabbedPane = createChartTabbedPane();
        add(chartTabbedPane, BorderLayout.CENTER);
    }

    private JTabbedPane createChartTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Styling for headers and panels
        Font headerFont = new Font("Segoe UI", Font.BOLD, 18);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        // Room Entrances Chart Tab
        JPanel roomEntrancesPanel = new JPanel();
        roomEntrancesPanel.setLayout(new BoxLayout(roomEntrancesPanel, BoxLayout.Y_AXIS));
        JLabel roomHeader = new JLabel("Room Entrance Distribution");
        roomHeader.setFont(headerFont);
        roomHeader.setBorder(padding);

        UtilDateModel roomDateModel = new UtilDateModel();
        Properties roomDateProps = new Properties();
        roomDateProps.put("text.today", "Today");
        roomDateProps.put("text.month", "Month");
        roomDateProps.put("text.year", "Year");
        JDatePanelImpl roomDatePanel = new JDatePanelImpl(roomDateModel, roomDateProps);
        roomDatePicker = new JDatePickerImpl(roomDatePanel, new DefaultFormatter());
        roomDatePicker.addActionListener(e -> updateRoomEntrancesChart());

        JPanel roomInputs = new JPanel(new GridLayout(2, 2, 5, 5));
        roomInputs.add(new JLabel("Building:"));
        roomInputs.add(buildingSelector);
        roomInputs.add(new JLabel("Date:"));
        roomInputs.add(roomDatePicker);

        roomEntrancesChartPanel = new ChartPanel(null);
        roomEntrancesChartPanel.setPreferredSize(new Dimension(500, 350));

        roomEntrancesPanel.add(roomHeader);
        roomEntrancesPanel.add(roomInputs);
        roomEntrancesPanel.add(roomEntrancesChartPanel);
        tabbedPane.addTab("Room Entrance Distribution", roomEntrancesPanel);

        JPanel unconfirmedPanel = new JPanel();
        unconfirmedPanel.setLayout(new BoxLayout(unconfirmedPanel, BoxLayout.Y_AXIS));
        JLabel unconfirmedHeader = new JLabel("Unconfirmed Entrances per Room");
        unconfirmedHeader.setFont(headerFont);
        unconfirmedHeader.setBorder(padding);

        unconfirmedEntrancesChartPanel = new ChartPanel(null);
        unconfirmedEntrancesChartPanel.setPreferredSize(new Dimension(500, 350));

        unconfirmedPanel.add(unconfirmedHeader);
        unconfirmedPanel.add(unconfirmedEntrancesChartPanel);
        tabbedPane.addTab("Daily Trend", unconfirmedPanel);

        // Enrollment Confirmation Chart Tab
        JPanel enrollmentPanel = new JPanel();
        enrollmentPanel.setLayout(new BoxLayout(enrollmentPanel, BoxLayout.Y_AXIS));
        JLabel enrollmentHeader = new JLabel("Hourly Enrollments");
        enrollmentHeader.setFont(headerFont);
        enrollmentHeader.setBorder(padding);

        JPanel enrollmentInputs = new JPanel();
        enrollmentInputs.setLayout(new BoxLayout(enrollmentInputs, BoxLayout.Y_AXIS));
        enrollmentInputs.add(createLabeledPanel("User:", userSelector));

        enrollmentConfirmationChartPanel = new ChartPanel(null);
        enrollmentConfirmationChartPanel.setPreferredSize(new Dimension(500, 350));

        enrollmentPanel.add(enrollmentHeader);
        enrollmentPanel.add(enrollmentInputs);
        enrollmentPanel.add(enrollmentConfirmationChartPanel);
        tabbedPane.addTab("Enrollment Confirmation", enrollmentPanel);

        JPanel lateControlPanel = new JPanel();
        lateControlPanel.setLayout(new BoxLayout(lateControlPanel, BoxLayout.Y_AXIS));
        JLabel lateControlHeader = new JLabel("User Enrollments");
        lateControlHeader.setFont(headerFont);
        lateControlHeader.setBorder(padding);

        UtilDateModel lateControlDateModel = new UtilDateModel();
        Properties lateControlDateProps = new Properties();
        lateControlDateProps.put("text.today", "Today");
        lateControlDateProps.put("text.month", "Month");
        lateControlDateProps.put("text.year", "Year");
        JDatePanelImpl lateControlDatePanel = new JDatePanelImpl(lateControlDateModel, lateControlDateProps);
        lateControlDatePicker = new JDatePickerImpl(lateControlDatePanel, new DefaultFormatter());
        lateControlDatePicker.addActionListener(e -> updateLateControlChart());

        JPanel lateControlInputs = new JPanel(new GridLayout(3, 2, 10, 10));
        lateControlInputs.add(new JLabel("User:"));
        lateControlInputs.add(userSelector);
        lateControlInputs.add(new JLabel("Date:"));
        lateControlInputs.add(lateControlDatePicker);
        lateControlInputs.add(new JLabel("Hour:"));
        lateControlInputs.add(hourSpinner);

        lateControlChartPanel = new ChartPanel(null);
        lateControlChartPanel.setPreferredSize(new Dimension(500, 350));

        lateControlPanel.add(lateControlHeader);
        lateControlPanel.add(lateControlInputs);
        lateControlPanel.add(lateControlChartPanel);
        tabbedPane.addTab("Late Control", lateControlPanel);

        return tabbedPane;
    }

    private JPanel createLabeledPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label);
        panel.add(component);
        return panel;
    }

    private void loadBuildings() {
        buildingService.getAllBuildings(buildings -> {
            buildingSelector.removeAllItems();
            buildings.forEach(building -> buildingSelector.addItem(building));
        }, this);
    }

    private void loadUsers() {
        userService.getAllUsers(users -> {
            userSelector.removeAllItems();
            users.forEach(user -> userSelector.addItem(user));
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

        Date selectedDate = (Date) roomDatePicker.getModel().getValue();
        LocalDate localDate = selectedDate != null ?
                selectedDate
                        .toInstant()
                        .atZone(systemDefault())
                        .toLocalDate() :
                now();
        enrollmentService.getNumberOfEntrancesToEachRoomOnDate(
                localDate,
                selectedBuilding.id(),
                (roomEntranceData) -> {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    roomEntranceData.forEach(entry -> {
                        dataset.addValue(entry.entranceCount(), "Entrances", entry.roomNumber());
                    });

                    JFreeChart chart = ChartFactory.createBarChart(
                            "Room Entrance Distribution",
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
                            "Unconfirmed Entrances per Room",
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
        UserDTO selectedUser = (UserDTO) userSelector.getSelectedItem();
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
                            "User Enrollment Confirmation Rate",
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
        UserDTO selectedUser = (UserDTO) userSelector.getSelectedItem();

        if (selectedUser == null) return;

        Date selectedDate = (Date) roomDatePicker.getModel().getValue();
        LocalDate localDate = selectedDate != null ?
                selectedDate
                        .toInstant()
                        .atZone(systemDefault())
                        .toLocalDate() :
                now();
        enrollmentService.getLateControlByUserAndRoom(
                localDate,
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
                            "Late Entries by Room",
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
        if (userSelector.getItemCount() > 0) {
            userSelector.setSelectedIndex(0);
        }
        if (roomSelector.getItemCount() > 0) {
            roomSelector.setSelectedIndex(0);
        }
//        datePicker.setDate(LocalDate.now());
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
}


