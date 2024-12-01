package com.example.gui.tabs;

import com.example.client.RoomService;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UpdateRoomRequest;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.valueOf;
import static javax.swing.JOptionPane.*;

public class UpdateBasicRoomDialog extends JDialog {

    private final RoomDTO room;
    private final RoomService roomService;
    private final Runnable onSuccess;

    public UpdateBasicRoomDialog(Frame owner, RoomDTO room, RoomService roomService, Runnable onSuccess) {
        super(owner, true);

        this.room = room;
        this.roomService = roomService;
        this.onSuccess = onSuccess;

        initComponents();
    }

    private void initComponents() {
        setTitle("Update room: " + room.roomNumber());

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Enter new room number:"));

        JTextField roomNumberField = new JTextField(room.roomNumber());

        panel.add(roomNumberField);

        panel.add(new JLabel("Enter new floor number:"));

        JSpinner floorSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));

        panel.add(floorSpinner);

        int result = JOptionPane.showConfirmDialog(
                getOwner(),
                panel,
                "Update Room",
                OK_CANCEL_OPTION,
                PLAIN_MESSAGE
        );

        if (result == OK_OPTION) {
            String newRoomNumber = roomNumberField.getText().trim();
            int newFloorNumber = (int) floorSpinner.getValue();

            if (newRoomNumber.isEmpty() || newFloorNumber < 0) {
                JOptionPane.showMessageDialog(
                        getOwner(),
                        "All fields are required.",
                        "Input Error",
                        ERROR_MESSAGE);
                return;
            }

            roomService.updateRoomWithId(room.roomId(),
                    new UpdateRoomRequest(newRoomNumber, newFloorNumber),
                    response -> {
                        JOptionPane.showMessageDialog(
                                getOwner(),
                                "Room updated successfully.",
                                "Success",
                                INFORMATION_MESSAGE);
                        onSuccess.run();
                    },
                    this);
        }
    }
}
