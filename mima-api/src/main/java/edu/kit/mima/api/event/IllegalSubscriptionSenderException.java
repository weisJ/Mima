package edu.kit.mima.api.event;

/**
 * Exception that indicates a service tried to send a notification to a channel he does not provide
 * for.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class IllegalSubscriptionSenderException extends RuntimeException {

    /**
     * Create new exception.
     *
     * @param message the exception message.
     */
    public IllegalSubscriptionSenderException(String message) {
        super(message);
    }
}
