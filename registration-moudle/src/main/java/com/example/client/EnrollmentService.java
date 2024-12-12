package com.example.client;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class EnrollmentService {

    private final EnrollmentClient enrollmentClient;

    public EnrollmentService(EnrollmentClient enrollmentClient) {
        this.enrollmentClient = enrollmentClient;
    }

    public void getNumberOfEntrancesToEachRoomOnDate(LocalDate date,
                                                     Long buildingId,
                                                     Consumer<List<Object[]>> onSuccess,
                                                     Component parentComponent) {
        BaseResourceWorker.execute(
                () -> enrollmentClient.getNumberOfEntrancesToEachRoomOnDate(date, buildingId),
                onSuccess,
                parentComponent
        );
    }

    public void getUnconfirmedEntrancesPerUserByRoom(Consumer<List<Object[]>> onSuccess,
                                                     Component parentComponent) {
        BaseResourceWorker.execute(
                enrollmentClient::getUnconfirmedEntrancesPerUserByRoom,
                onSuccess,
                parentComponent
        );
    }

    public void getUserEnrollmentConfirmationRate(Long userId,
                                                  Consumer<List<Object[]>> onSuccess,
                                                  Component parentComponent) {
        BaseResourceWorker.execute(
                () -> enrollmentClient.getUserEnrollmentConfirmationRate(userId),
                onSuccess,
                parentComponent
        );
    }

    public void getLateControlByUserAndRoom(LocalDate date,
                                            Long userId,
                                            int expectedHour,
                                            Consumer<List<Object[]>> onSuccess,
                                            Component parentComponent) {
        BaseResourceWorker.execute(
                () -> enrollmentClient.getLateControlByUserAndRoom(date, userId, expectedHour),
                onSuccess,
                parentComponent
        );
    }
}
