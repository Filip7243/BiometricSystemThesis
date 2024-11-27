package com.example.client;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BaseResourceWorker<T> extends SwingWorker<T, Void> {
//    private static final Logger LOGGER = Logger.getLogger(ResourceWorker.class.getName());

    private final Callable<T> backgroundTask;
    private final Consumer<T> successHandler;
    private final Consumer<Exception> errorHandler;

    /**
     * Comprehensive constructor for flexible resource operations
     *
     * @param backgroundTask The main task to be executed in background
     * @param successHandler Callback for successful completion
     * @param errorHandler   Callback for error handling
     */
    public BaseResourceWorker(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Consumer<Exception> errorHandler) {
        this.backgroundTask = backgroundTask;
        this.successHandler = successHandler;
        this.errorHandler = errorHandler;
    }

    /**
     * Simplified constructor with default error handling
     *
     * @param backgroundTask  The main task to be executed in background
     * @param successHandler  Callback for successful completion
     * @param parentComponent Parent component for error dialogs
     */
    public BaseResourceWorker(
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
                )
        );
    }

    @Override
    protected T doInBackground() throws Exception {
//        LOGGER.info("Starting background task");
        return backgroundTask.call();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            if (successHandler != null) {
                SwingUtilities.invokeLater(() -> successHandler.accept(result));
            }
        } catch (Exception ex) {
//            LOGGER.log(Level.SEVERE, "Background task failed", ex);
            if (errorHandler != null) {
                SwingUtilities.invokeLater(() -> errorHandler.accept(ex));
            }
        }
    }

    /**
     * Convenience method to execute a background task
     */
    public static <T> BaseResourceWorker<T> execute(
            Callable<T> backgroundTask,
            Consumer<T> successHandler,
            Component parentComponent) {

        BaseResourceWorker<T> worker = new BaseResourceWorker<>(
                backgroundTask, successHandler, parentComponent
        );
        worker.execute();
        return worker;
    }
}
