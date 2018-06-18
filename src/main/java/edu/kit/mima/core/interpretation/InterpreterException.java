package edu.kit.mima.core.interpretation;

/**
 * Gets thrown by {@link Interpreter}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class InterpreterException extends IllegalArgumentException {


    /**
     * Interpreter Expression
     *
     * @param message exception message
     */
    public InterpreterException(String message) {
        super(message);
    }
}
