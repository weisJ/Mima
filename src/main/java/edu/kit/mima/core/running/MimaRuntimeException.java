package edu.kit.mima.core.running;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRuntimeException extends RuntimeException {

    private final String message;

    /**
     * Create new MimaRuntimeException
     *
     * @param message exception message
     */
    public MimaRuntimeException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
