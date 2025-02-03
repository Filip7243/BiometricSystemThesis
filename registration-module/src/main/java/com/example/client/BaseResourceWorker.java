package com.example.client;


import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Klasa bazowa do obsługi operacji wykonywanych w tle przy użyciu SwingWorker.
 *
 * @param <T> Typ wejściowy (nieużywany, ale wymagany przez SwingWorker)
 * @param <R> Typ zwracany przez zadanie
 */
public class BaseResourceWorker<T, R> extends SwingWorker<R, Void> {
    private final Callable<R> requestTask;
    private final Consumer<R> responseHandler;
    private final Consumer<Exception> errorHandler;

    /**
     * Konstruktor umożliwiający elastyczne wykonywanie operacji API w tle.
     *
     * @param requestTask     Główne zadanie do wykonania w tle
     * @param responseHandler Callback dla pomyślnego zakończenia
     * @param errorHandler    Callback do obsługi błędów
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
     * Uproszczony konstruktor z domyślną obsługą błędów.
     *
     * @param requestTask     Główne zadanie do wykonania w tle
     * @param responseHandler Callback dla pomyślnego zakończenia
     * @param parentComponent Komponent nadrzędny do wyświetlania okien dialogowych w przypadku błędów
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

    /**
     * Metoda wykonywana w tle, uruchamia zadanie przekazane w konstruktorze.
     *
     * @return Wynik operacji
     * @throws Exception Jeśli wystąpi błąd podczas wykonywania zadania
     */
    @Override
    protected R doInBackground() throws Exception {
        return requestTask.call();
    }

    /**
     * Metoda wywoływana po zakończeniu operacji w tle.
     * Obsługuje poprawne zakończenie oraz błędy.
     */
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
     * Metoda pomocnicza do uruchamiania zadania w tle.
     *
     * @param requestTask     Główne zadanie do wykonania w tle
     * @param responseHandler Callback dla pomyślnego zakończenia
     * @param parentComponent Komponent nadrzędny do obsługi błędów
     * @param <T>             Typ wejściowy (nieużywany, ale wymagany przez SwingWorker)
     * @param <R>             Typ zwracany przez zadanie
     * @return Instancja {@link BaseResourceWorker} uruchomiona do wykonania operacji
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
