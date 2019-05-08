package edu.kit.mima.core;

import edu.kit.mima.core.interpretation.Value;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Code CodeRunner interface.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface CodeRunner {

    @NotNull
    Debugger debugger();

    void start(Consumer<Value> callback);

    void stop();

    boolean isRunning();
}
