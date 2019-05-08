package edu.kit.mima.api.event;

import org.jetbrains.annotations.Contract;

/**
 * Simple implementation of {@link AbstractSubscriber}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SimpleSubscriber extends AbstractSubscriber {

    private final SubscriberEventHandler handler;

    @Contract(pure = true)
    public SimpleSubscriber(
            final Class classFilter, final Object invokerFilter, final SubscriberEventHandler handler) {
        super(classFilter, invokerFilter);
        this.handler = handler;
    }

    @Contract(pure = true)
    public SimpleSubscriber(final Class classFilter, final SubscriberEventHandler handler) {
        this(classFilter, null, handler);
    }

    @Contract(pure = true)
    public SimpleSubscriber(final Object invokerFilter, final SubscriberEventHandler handler) {
        this(null, invokerFilter, handler);
    }

    @Contract(pure = true)
    public SimpleSubscriber(final SubscriberEventHandler handler) {
        this(null, null, handler);
    }

    @Override
    public <T> void notifySubscription(final String identifier, final T value) {
        handler.notifySubscription(identifier, value);
    }
}
