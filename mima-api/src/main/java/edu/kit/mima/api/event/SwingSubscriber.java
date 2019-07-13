package edu.kit.mima.api.event;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class SwingSubscriber<T> extends SimpleSubscriber<T> {
    public SwingSubscriber(final Class<?> classFilter, final Object invokerFilter, final SubscriberEventHandler<T> handler) {
        super(classFilter, invokerFilter, handler);
    }

    public SwingSubscriber(final Class<?> classFilter, final SubscriberEventHandler<T> handler) {
        super(classFilter, handler);
    }

    public SwingSubscriber(final Object invokerFilter, final SubscriberEventHandler<T> handler) {
        super(invokerFilter, handler);
    }

    public SwingSubscriber(final SubscriberEventHandler<T> handler) {
        super(handler);
    }

    @Override
    public <T1> void notifySubscription(final String identifier, final T1 value) {
        SwingUtilities.invokeLater(() -> super.notifySubscription(identifier, value));
    }
}
