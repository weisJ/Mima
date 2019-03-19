package edu.kit.mima.core.interpretation;

/**
 * Gets thrown by {@link Interpreter} if execution fails.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class InterpreterException extends IllegalArgumentException {


    /**
     * Create new {@link InterpreterException}.
     *
     * @param message exception message
     */
    public InterpreterException(final String message) {
        super(message);
    }
}
