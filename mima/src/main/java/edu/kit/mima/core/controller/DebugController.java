package edu.kit.mima.core.controller;

import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.parsing.token.Token;

/**
 * DebugController is used for controlling execution flow in {@link Interpreter}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface DebugController {

    /**
     * Pause the task.
     */
    void pause();

    /**
     * Resume the task.
     */
    void resume();

    /**
     * Start the task.
     */
    void start();

    /**
     * Stop the task.
     */
    void stop();

    /**
     * Action to perform after instruction.
     *
     * @param nextInstruction next Instruction
     */
    void afterInstruction(Token nextInstruction);
}
