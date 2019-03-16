package edu.kit.mima.core.running;

import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.gui.components.Breakpoint;
import edu.kit.mima.gui.observing.Observable;

import java.util.function.Consumer;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Debugger extends Observable {
    String RUNNING_PROPERTY = "running";
    String PAUSE_PROPERTY = "paused";

    void start(Consumer<Value> callback);

    void pause();

    void resume();

    void step();

    boolean isRunning();

    boolean isPaused();

    void setBreakpoints(Breakpoint[] breakpoints);

    void addPauseListener(PauseListener listener);

    void removePauseListener(PauseListener listener);
}
