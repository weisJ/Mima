package edu.kit.mima.api.history;

import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SubscriptionService;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class SubscribableHistory<T> extends LinkedHistory<T> {

    private static final SubscriptionService<Integer> SUBSCRIPTION_SERVICE = new SubscriptionService<>();

    static {
        SubscriptionManager.getCurrentManager().offerSubscription(SUBSCRIPTION_SERVICE,
                                                                  LENGTH_PROPERTY,
                                                                  POSITION_PROPERTY);
    }

    /**
     * Create LinkedHistory.
     *
     * @param capacity maximum capacity
     */
    public SubscribableHistory(final int capacity) {
        super(capacity);
    }

    protected void fireLengthChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(LENGTH_PROPERTY, length(), this);
    }

    protected void firePositionChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(POSITION_PROPERTY, head, this);
    }
}
