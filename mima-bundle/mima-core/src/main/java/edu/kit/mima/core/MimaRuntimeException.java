package edu.kit.mima.core;

/**
 * Runtime Exception that occurs during Mima Execution.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRuntimeException extends RuntimeException {

    private final String message;

    /**
     * Create new MimaRuntimeException.
     *
     * @param message exception message
     */
    public MimaRuntimeException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
