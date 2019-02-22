package edu.kit.mima.core.query;

/**
 * Throw if a data-structure gets an illegal request
 *
 * @author Jannis Weis
 */
public class IllegalRequestException extends RuntimeException {
    /**
     * Gets thrown if the Database has to process an illegal request
     *
     * @param message Message pf this exception
     */
    public IllegalRequestException(String message) {
        super(message);
    }
}
