package edu.kit.mima.api.event;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Subscription Manger for registering new subscriptions offered and subscribing to channels.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SubscriptionManager {

    private static SubscriptionManager instance = new SubscriptionManager();

    private Set<String> allServices;
    private Map<SubscriptionService, Set<String>> offeredSubscriptions;
    private Map<String, List<Subscriber>> subscriberMap;

    @Contract(pure = true)
    private SubscriptionManager() {
        offeredSubscriptions = new HashMap<>();
        subscriberMap = new HashMap<>();
        allServices = new HashSet<>();
    }

    @Contract(pure = true)
    public static SubscriptionManager getCurrentManager() {
        return instance;
    }

    /**
     * Register an subscription that others can subscribe to. IF the Subscription service send a
     * notify event all subscribers will receive it.
     *
     * </p>Different subscriptions services may offer the same subscription. This allows for
     * subscribers to listen to the events of different objects by the same identification.
     *
     * @param service     the subscription service.
     * @param identifiers the subscription identifier.
     * @param <T>         the subscription value type.
     */
    public <T> void offerSubscription(@NotNull final SubscriptionService<T> service,
                                      @NotNull final String... identifiers) {
        if (!offeredSubscriptions.containsKey(service)) {
            offeredSubscriptions.put(service, new HashSet<>());
        }
        var list = offeredSubscriptions.get(service);
        service.setSendDelegate(this::notifySubscribers);
        var identList = Arrays.asList(identifiers);
        list.addAll(identList);
        allServices.addAll(identList);
        service.initService(identifiers);
    }

    /**
     * Subscribe an subscriber to a subscription channel. It is possible to subscribe to an channel
     * a priori. If the channel ever gets created the subscriber will be automatically be subscribed
     * to it.
     *
     * @param subscriber  the subscriber to subscribe.
     * @param identifiers the identifier for the subscription channel.
     */
    public void subscribe(final Subscriber subscriber, @NotNull final String... identifiers) {
        for (var ident : identifiers) {
            if (!subscriberMap.containsKey(ident)) {
                subscriberMap.put(ident, new ArrayList<>());
            }
            subscriberMap.get(ident).add(subscriber);
        }
    }

    /**
     * Subscribe an subscriber to a subscription channel. If the specified channel does not exist
     * nothing happens.
     *
     * @param subscriber  the subscriber to subscribe.
     * @param identifiers the identifier for the subscription channel.
     */
    public void weakSubscribe(final Subscriber subscriber, @NotNull final String... identifiers) {
        for (var ident : identifiers) {
            if (!allServices.contains(ident)) {
                continue;
            }
            if (!subscriberMap.containsKey(ident)) {
                subscriberMap.put(ident, new ArrayList<>());
            }
            subscriberMap.get(ident).add(subscriber);
        }
    }

    private <T> void notifySubscribers(final String identification, final T value,
                                       final SubscriptionService<T> service, final Class[] owner,
                                       final Object invoker) {
        /*
         * Ensure the notification sender also is the provider of the subscription.
         */
        if (!offeredSubscriptions.get(service).contains(identification)) {
            throw new IllegalSubscriptionSenderException(
                    service + " does not provide \"" + identification + "\"");
        }
        for (var subscriber : Optional.ofNullable(subscriberMap.get(identification)).orElse(
                List.of())) {
            if (subscriber.useOwnerFilter() && !Arrays.asList(owner).contains(
                    subscriber.getOwnerFilter(identification))
                || subscriber.useInvokeFilter() && invoker != subscriber.getInvokerFilter(
                    identification)) {
                continue;
            }
            subscriber.notifySubscription(identification, value);
        }
    }

}
