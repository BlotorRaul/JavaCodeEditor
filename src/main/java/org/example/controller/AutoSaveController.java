package org.example.controller;

import org.example.application.AutoSaveService;

import javax.swing.*;


/**
 * A controller class that manages the auto-save functionality for a {@link JTextPane}.
 * <p>
 * This class abstracts the implementation details of the {@link AutoSaveService}
 * and provides simple methods to start and stop auto-saving.
 * <p>
 * Note: The auto-save interval is set to 2 minutes by default. You can modify
 * this behavior in the {@link AutoSaveService} constructor if needed.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class AutoSaveController {

    private final AutoSaveService autoSaveService;

    /**
     * Constructs an `AutoSaveController` and initializes an {@link AutoSaveService}
     * for the provided {@link JTextPane}.
     *
     * @param textPane The {@link JTextPane} whose content will be automatically saved.
     */
    public AutoSaveController(JTextPane textPane) {
        this.autoSaveService = new AutoSaveService(textPane, 2);
    }

    /**
     * Starts the auto-save functionality if it is not already running.
     */
    public void startAutoSave() {
        autoSaveService.start();
    }

    /**
     * Stops the auto-save functionality if it is currently running.
     */
    public void stopAutoSave() {
        autoSaveService.stop();
    }
}
