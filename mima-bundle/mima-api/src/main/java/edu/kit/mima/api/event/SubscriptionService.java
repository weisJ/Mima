package edu.kit.mima.api.event;

import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * Notification Service for Subscriptions.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SubscriptionService<T> {

    private final Class<?>[] owner;
    private SubscriptionDelegate<T> sendDelegate;

    @Contract(pure = true)
    public SubscriptionService(final Class<?>... owner) {
        this.owner = owner == null ? new Class[0] : owner;
    }

    void setSendDelegate(final SubscriptionDelegate<T> sendDelegate) {
        this.sendDelegate = sendDelegate;
    }

    /**
     * Send a notification event.
     *
     * @param identification the identification of the event.
     * @param value          the value of the event.
     * @param invoker        the invoker.
     */
    public final void notifyEvent(final String identification, final T value, final Object invoker) {
        /*
         * This function may not be overwritten to prevent non subscription services to
         * send notifications.
         */
        Optional.ofNullable(sendDelegate)
                .ifPresent(sd -> sd.sendNotify(identification, value, this, owner, invoker));
    }

    void initService(final String... identifiers) {
    }

    interface SubscriptionDelegate<T> {
        void sendNotify(
                final String identification,
                T value,
                final SubscriptionService<T> service,
                final Class<?>[] serviceOwner,
                final Object invoker);
    }
}
