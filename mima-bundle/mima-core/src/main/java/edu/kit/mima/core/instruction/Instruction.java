package edu.kit.mima.core.instruction;

import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.environment.Environment;

import java.util.List;
import java.util.function.Consumer;

/**
 * Instruction interface for Mima.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Instruction {

    /**
     * Apply the function.
     *
     * @param arguments   function arguments array
     * @param environment runtime environment
     * @param callback    callback for method return value
     */
    void apply(List<Value<?>> arguments, Environment environment, Consumer<Value<?>> callback);
}
