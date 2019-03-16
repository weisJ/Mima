package edu.kit.mima.core.running;

import edu.kit.mima.gui.components.Breakpoint;
import edu.kit.mima.gui.observing.Observable;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Debugger extends Observable {
    String RUNNING_PROPERTY = "running";
    String PAUSE_PROPERTY = "paused";

    void start();

    void pause();

    void resume();

    void step();

    boolean isRunning();

    boolean isPaused();

    void setBreakpoints(Breakpoint[] breakpoints);

    void addPauseListener(PauseListener listener);

    void removePauseListener(PauseListener listener);
}
