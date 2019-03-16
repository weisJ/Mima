package edu.kit.mima.core.controller;

import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.gui.logging.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ThreadDebugController implements DebugController {

    private final Object lock = new Object();
    private Set<Integer> breaks;
    private Thread workingThread;
    private boolean isActive;
    private boolean autoPause;

    /**
     * Create new ThreadDebugController.
     */
    public ThreadDebugController() {
        isActive = false;
        autoPause = false;
        breaks = new HashSet<>();
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
        autoPause = false;
        synchronized (lock) {
            isActive = false;
        }
        try {
            workingThread.join();
        } catch (InterruptedException ignored) {/*doesn't matter thread should die*/}
    }

    @Override
    public void afterInstruction(Token currentInstruction) {
        if ((autoPause || breaks.contains(currentInstruction.getFilePos()))
                && currentInstruction.getType() != TokenType.PROGRAM) {
            pause();
        }
    }

    /**
     * Set break points.
     *
     * @param breaks break point collection.
     */
    public void setBreaks(Collection<Integer> breaks) {
        this.breaks.clear();
        this.breaks.addAll(breaks);
    }

    /**
     * Sets whether the thread should be automatically paused after
     * each step;
     *
     * @param autoPause true if debugger should auto pause after each step
     */
    public void setAutoPause(boolean autoPause) {
        this.autoPause = autoPause;
    }
}
