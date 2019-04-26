package edu.kit.mima.api.event;

/**
 * SubscriberEventHandler interface for Subscription events.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface SubscriberEventHandler {

    void notifySubscription(String identifier, Object value);
}
