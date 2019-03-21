package edu.kit.mima.core.interpretation;

/**
 * Handler for Exceptions thrown during execution.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface ExceptionHandler {

    /**
     * Notify the listener of the exception.
     *
     * @param e exception to notify about
     */
    void notifyException(Exception e);
}
