package edu.kit.mima.core;

import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.core.interpretation.Value;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Debugging interface for program execution.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Debugger {
    String RUNNING_PROPERTY = "debugger.running";
    String PAUSE_PROPERTY = "debugger.paused";

    void start(Consumer<Value<?>> callback, Collection<Breakpoint> breakpoints);

    void pause();

    void resume();

    void step();

    boolean isRunning();

    boolean isPaused();
}
