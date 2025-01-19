package org.example.application;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service that automatically saves the content of a {@link JTextPane} to a file
 * at regular intervals (default is 2 minutes).
 * <p>
 * The service runs in a separate thread to avoid blocking the main application thread.
 * <p>
 * Example usage:
 * <pre>
 *     JTextPane textPane = new JTextPane();
 *     AutoSaveService autoSaveService = new AutoSaveService(textPane, 2);
 *     autoSaveService.start();
 *
 *     // Stop the service when no longer needed
 *     autoSaveService.stop();
 * </pre>
 * <p>
 * Note: The saved file is hardcoded as "autosave_output.txt" in the current working directory.
 * You can modify the {@link #saveToFile()} method to change the file location or format.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class AutoSaveService {

    private ScheduledExecutorService scheduler;

    private final JTextPane textPane;

    private final int saveIntervalMinutes;

    /**
     * Creates an AutoSaveService instance with the specified {@link JTextPane} and save interval.
     *
     * @param textPane            The {@link JTextPane} whose content will be saved.
     * @param saveIntervalMinutes The interval at which the content will be saved (in minutes).
     */
    public AutoSaveService(JTextPane textPane, int saveIntervalMinutes) {
        this.textPane = textPane;
        this.saveIntervalMinutes = saveIntervalMinutes;
    }

    /**
     * Starts the auto-save service.
     * <p>
     * A single-threaded executor is created, and the save task is scheduled to run
     * at a fixed interval. If the service is already running, this method does nothing.
     */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;//do nothing
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            saveToFile();
        }, 0, saveIntervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * Stops the auto-save service.
     * <p>
     * This method shuts down the executor service, preventing any further scheduled tasks.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * Saves the content of the {@link JTextPane} to a text file.
     * <p>
     * The content is saved to a file named "autosave_output.txt" in the current working directory.
     * If an error occurs during the save process, the stack trace is printed.
     */
    private void saveToFile() {
        String content = textPane.getText();

        try (PrintWriter out = new PrintWriter(new FileWriter("autosave_output.txt"))) {
            out.println(content);
            System.out.println("[AutoSaveService] A salvat conținutul cu succes.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
