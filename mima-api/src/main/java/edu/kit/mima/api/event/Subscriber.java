package edu.kit.mima.api.event;

/**
 * Subscriber interface for {@link SubscriptionManager}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface Subscriber {

    <T> void notifySubscription(final String identifier, final T value);

    boolean useOwnerFilter();

    boolean useInvokeFilter();

    Class getOwnerFilter();

    Object getInvokerFilter();
}
