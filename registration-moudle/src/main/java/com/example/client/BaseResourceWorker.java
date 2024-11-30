package com.example.client;


import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BaseResourceWorker<T, R> extends SwingWorker<R, Void> {
    private final Callable<R> requestTask;
    private final Consumer<R> responseHandler;
    private final Consumer<Exception> errorHandler;

    /**
     * Comprehensive constructor for flexible API request operations
     *
     * @param requestTask     The main task to be executed in background
     * @param responseHandler Callback for successful completion
     * @param errorHandler    Callback for error handling
     */
    public BaseResourceWorker(
            Callable<R> requestTask,
            Consumer<R> responseHandler,
            Consumer<Exception> errorHandler) {
        this.requestTask = requestTask;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
    }

    /**
     * Simplified constructor with default error handling
     *
     * @param requestTask     The main task to be executed in background
     * @param responseHandler Callback for successful completion
     * @param parentComponent Parent component for error dialogs
     */
    public BaseResourceWorker(
            Callable<R> requestTask,
            Consumer<R> responseHandler,
            Component parentComponent) {
        this(
                requestTask,
                responseHandler,
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
    protected R doInBackground() throws Exception {
        return requestTask.call();
    }

    @Override
    protected void done() {
        try {
            R result = get();
            if (responseHandler != null) {
                SwingUtilities.invokeLater(() -> responseHandler.accept(result));
            }
        } catch (Exception ex) {
            if (errorHandler != null) {
                SwingUtilities.invokeLater(() -> errorHandler.accept(ex));
            }
        }
    }

    /**
     * Convenience method to execute a background task
     */
    public static <T, R> BaseResourceWorker<T, R> execute(
            Callable<R> requestTask,
            Consumer<R> responseHandler,
            Component parentComponent) {

        BaseResourceWorker<T, R> worker = new BaseResourceWorker<>(
                requestTask, responseHandler, parentComponent
        );
        worker.execute();
        return worker;
    }
}
