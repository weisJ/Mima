package edu.kit.mima.core.controller;

import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.core.interpretation.SimpleBreakpoint;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Implementation of {@link DebugController} using a threat to handle control flow.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ThreadDebugController implements DebugController {

    private final Thread workingThread;
    @Nullable
    private Collection<Breakpoint> breaks;
    private boolean isActive;
    private boolean autoPause;
    private boolean shouldDie;

    /**
     * Create new ThreadDebugController.
     *
     * @param workingThread the working thread.
     */
    @Contract(pure = true)
    public ThreadDebugController(final Thread workingThread) {
        isActive = false;
        autoPause = false;
        breaks = new HashSet<>();
        this.workingThread = workingThread;
    }

    /**
     * Returns whether the thread is currently active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void pause() {
        synchronized (workingThread) {
            isActive = false;
            try {
                workingThread.wait();
            } catch (@NotNull final InterruptedException e) {
                isActive = true;
            }
        }
    }

    @Override
    public void resume() {
        if (workingThread == null) {
            return;
        }
        synchronized (workingThread) {
            isActive = true;
            workingThread.interrupt();
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
        shouldDie = true;
        synchronized (workingThread) {
            isActive = false;
            try {
                workingThread.interrupt();
                workingThread.join();
            } catch (@NotNull final InterruptedException ignored) {
                /*doesn't matter thread should die*/
            }
        }
        shouldDie = false;
    }

    @Override
    public void afterInstruction(final Token<?> currentInstruction) {
        if (shouldDie || breaks == null) {
            return;
        }
        if (autoPause
                    || Optional.ofNullable(currentInstruction)
                               .map(
                                       t ->
                                               breaks.contains(new SimpleBreakpoint(t.getOffset()))
                                                       && t.getType() != TokenType.PROGRAM)
                               .orElse(false)) {
            pause();
        }
    }

    /**
     * Set break points.
     *
     * @param breaks break point collection.
     */
    public void setBreaks(@NotNull final Collection<Breakpoint> breaks) {
        this.breaks = breaks;
    }

    /**
     * Sets whether the thread should be automatically paused after each step.
     *
     * @param autoPause true if debugger should auto pause after each step
     */
    public void setAutoPause(final boolean autoPause) {
        this.autoPause = autoPause;
    }
}
