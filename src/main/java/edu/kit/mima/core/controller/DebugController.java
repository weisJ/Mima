package edu.kit.mima.core.controller;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface DebugController {

    /**
     * Pause the task
     */
    void pause();

    /**
     * Resume the task
     */
    void resume();

    /**
     * Start the task
     */
    void start();

    /**
     * Stop the task
     */
    void stop();
}
