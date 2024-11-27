package com.example.client;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResourceWorker<T> extends SwingWorker<Optional<T>, Void> {
    //    private static final Logger LOGGER = Logger.getLogger(ResourceWorker.class.getName());
    private final Callable<T> backgroundTask;
    private final Consumer<T> successHandler;
    private final Consumer<Exception> errorHandler;
    private final Predicate<T> successPredicate;

    public ResourceWorker(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Consumer<Exception> errorHandler,
            Predicate<T> successPredicate) {
        this.backgroundTask = backgroundTask;
        this.successHandler = successHandler;
        this.errorHandler = errorHandler;
        this.successPredicate = successPredicate != null
                ? successPredicate
                : Objects::nonNull;
    }

    public ResourceWorker(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Component parentComponent) {
        this(
                backgroundTask,
                successHandler,
                ex -> SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                parentComponent,
                                "Operation failed: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        )
                ),
                null
        );
    }

    public ResourceWorker(Callable<T> backgroundTask,
                          Consumer<T> successHandler,
                          Predicate<T> successPredicate
    ) {
        this.backgroundTask = backgroundTask;
        this.successHandler = successHandler;
        this.successPredicate = successPredicate;
        this.errorHandler = ex -> {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            "Operation failed: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    )
            );
        };
    }

    @Override
    protected Optional<T> doInBackground() throws Exception {
//        LOGGER.info("Starting background task");
        T result = backgroundTask.call();
        return Optional.ofNullable(result);
    }

    @Override
    protected void done() {
        try {
            Optional<T> optionalResult = get();

            optionalResult.ifPresentOrElse(
                    result -> {
                        // Check if result passes the success predicate
                        if (successPredicate.test(result)) {
                            if (successHandler != null) {
                                SwingUtilities.invokeLater(() -> successHandler.accept(result));
                            }
                        } else {
                            // Handle case where result doesn't meet success criteria
//                            LOGGER.warning("Operation completed but result did not meet success criteria");
                            showWarningDialog("Operation completed with unexpected result");
                        }
                    },
                    () -> {
                        // Handle null result
//                        LOGGER.warning("Operation returned null result");
                        showWarningDialog("Operation did not return a valid result");
                    }
            );
        } catch (Exception ex) {
//            LOGGER.log(Level.SEVERE, "Background task failed", ex);
            if (errorHandler != null) {
                SwingUtilities.invokeLater(() -> errorHandler.accept(ex));
            }
        }
    }

    private void showWarningDialog(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                )
        );
    }

    public static <T> ResourceWorker<T> execute(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Component parentComponent) {
        ResourceWorker<T> worker = new ResourceWorker<>(
                backgroundTask, successHandler, parentComponent
        );
        worker.execute();

        return worker;
    }

    public static <T> ResourceWorker<T> execute(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Consumer<Exception> errorHandler,
            Predicate<T> successPredicate) {

        ResourceWorker<T> worker = new ResourceWorker<>(
                backgroundTask,
                successHandler,
                errorHandler,
                successPredicate
        );
        worker.execute();
        return worker;
    }

    public static BaseResourceWorker<Void> executeVoid(
            Runnable backgroundTask,
            Runnable successHandler,
            Component parentComponent
    ) {
        BaseResourceWorker<Void> worker = new BaseResourceWorker<>(
                () -> {
                    backgroundTask.run();
                    return null;
                },
                result -> {
                    if (successHandler != null) {
                        successHandler.run();
                    }
                },
                parentComponent
        );
        worker.execute();
        return worker;
    }
}
