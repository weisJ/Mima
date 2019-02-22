package edu.kit.mima.core.controller;

import edu.kit.mima.gui.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ThreadDebugController implements DebugController {

    private final Object lock = new Object();
    private final List<Runnable> beforeStop;
    private Thread workingThread;
    private boolean isActive;

    /**
     * Create new ThreadDebugController.
     */
    public ThreadDebugController() {
        beforeStop = new ArrayList<>();
        isActive = false;
    }

    /**
     * Set the working thread.
     *
     * @param workingThread working thread to control.
     */
    public void setWorkingThread(Thread workingThread) {
        this.workingThread = workingThread;
        isActive = false;
    }

    /**
     * Add action to be executed before thread is stopped.
     *
     * @param handler handler to add
     */
    public void addStopHandler(Runnable handler) {
        beforeStop.add(handler);
    }

    /**
     * Remove action that is currently executed before thread is stopped.
     *
     * @param handler handler to remove
     * @return true if handler was removed successfully
     */
    public boolean removeStopHandler(Runnable handler) {
        return beforeStop.remove(handler);
    }

    /**
     * Returns whether the thread is currently active
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void pause() {
        synchronized (lock) {
            isActive = false;
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Logger.error(e.getMessage());
                isActive = true;
            }
        }
    }

    @Override
    public void resume() {
        if (workingThread == null) {
            return;
        }
        synchronized (lock) {
            isActive = true;
            lock.notify();
        }
    }

    @Override
    public void start() {
        if (workingThread == null) {
            return;
        }
        isActive = true;
        workingThread.start();
    }

    @Override
    public void stop() {
        if (workingThread == null) {
            return;
        }
        synchronized (lock) {
            isActive = false;
            for (Runnable runnable : beforeStop) {
                runnable.run();
            }
            lock.notify();
        }
        try {
            workingThread.join();
        } catch (InterruptedException ignored) { /*doesn't matter thread should die*/}
    }
}
