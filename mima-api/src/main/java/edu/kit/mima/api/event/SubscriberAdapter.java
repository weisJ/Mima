package edu.kit.mima.api.event;

/**
 * Adapter for {@link Subscriber} interface.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class SubscriberAdapter implements Subscriber {
    @Override
    public <T> void notifySubscription(final String identifier, final T value) {
    }

    @Override
    public boolean useOwnerFilter() {
        return false;
    }

    @Override
    public boolean useInvokeFilter() {
        return false;
    }

    @Override
    public Class getOwnerFilter(final String ident) {
        return null;
    }

    @Override
    public Object getInvokerFilter(final String ident) {
        return null;
    }
}
