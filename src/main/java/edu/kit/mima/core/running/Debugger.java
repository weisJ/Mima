package edu.kit.mima.core.running;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Debugger {

    void pause();

    void resume();

    void step();
}
