package edu.kit.mima.api.event;

import org.jetbrains.annotations.Contract;

/**
 * Simple implementation of {@link AbstractSubscriber}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SimpleSubscriber<K> extends AbstractSubscriber {

    private final SubscriberEventHandler<K> handler;

    @Contract(pure = true)
    public SimpleSubscriber(
            final Class<?> classFilter, final Object invokerFilter, final SubscriberEventHandler<K> handler) {
        super(classFilter, invokerFilter);
        this.handler = handler;
    }

    @Contract(pure = true)
    public SimpleSubscriber(final Class<?> classFilter, final SubscriberEventHandler<K> handler) {
        this(classFilter, null, handler);
    }

    @Contract(pure = true)
    public SimpleSubscriber(final Object invokerFilter, final SubscriberEventHandler<K> handler) {
        this(null, invokerFilter, handler);
    }

    @Contract(pure = true)
    public SimpleSubscriber(final SubscriberEventHandler<K> handler) {
        this(null, null, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void notifySubscription(final String identifier, final T value) {
        handler.notifySubscription(identifier, (K) value);
    }
}
