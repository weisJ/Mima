package edu.kit.mima.api.event;

import org.jetbrains.annotations.Contract;

/**
 * Default implementation for a subscriber.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class AbstractSubscriber implements Subscriber {

    private final Class classFilter;
    private final Object invokerFilter;

    @Contract(pure = true)
    public AbstractSubscriber(final Class classFilter, final Object invokerFilter) {
        this.invokerFilter = invokerFilter;
        this.classFilter = classFilter;
    }

    @Contract(pure = true)
    public AbstractSubscriber(final Class classFilter) {
        this(classFilter, null);
    }

    @Contract(pure = true)
    public AbstractSubscriber(final Object invokerFilter) {
        this(null, invokerFilter);
    }

    @Contract(pure = true)
    public AbstractSubscriber() {
        this(null, null);
    }

    @Override
    public abstract <T> void notifySubscription(String identifier, T value);

    @Override
    public boolean useOwnerFilter() {
        return classFilter != null;
    }

    @Override
    public boolean useInvokeFilter() {
        return invokerFilter != null;
    }

    @Override
    public Class getOwnerFilter(final String identification) {
        return classFilter;
    }

    @Override
    public Object getInvokerFilter(final String identification) {
        return invokerFilter;
    }
}
