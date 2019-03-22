package edu.kit.mima.core;

import edu.kit.mima.core.interpretation.Value;

import java.util.function.Consumer;

/**
 * Code CodeRunner interface.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface CodeRunner {

    Debugger debugger();

    void start(Consumer<Value> callback);

    void stop();

    boolean isRunning();
}
