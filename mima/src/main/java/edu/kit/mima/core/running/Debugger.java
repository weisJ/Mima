package edu.kit.mima.core.running;

import edu.kit.mima.gui.observing.Observable;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Debugger extends Observable {
    String RUNNING_PROPERTY = "running";

    void pause();

    void resume();

    void step();

    boolean isRunning();
}
