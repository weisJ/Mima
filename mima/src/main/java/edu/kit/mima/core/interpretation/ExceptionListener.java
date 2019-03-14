package edu.kit.mima.core.interpretation;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface ExceptionListener {

    /**
     * Notify the listener of the exception
     *
     * @param e exception to notify about
     */
    void notifyException(Exception e);
}
