package edu.kit.mima.api.event;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Subscription service bridge for swing components. The service creator is responsible for not
 * registering this service for events that give back the wrong type of value. THis Service only
 * sends the new value at property change.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class BridgeSubscriptionService<T> extends SubscriptionService<T> implements
        PropertyChangeListener {

    private final Object observed;

    public <K extends JComponent> BridgeSubscriptionService(@NotNull final K observed) {
        super(observed.getClass());
        this.observed = observed;
    }


    @Override
    void initService(@NotNull final String... identifiers) {
        for (var s : identifiers) {
                ((JComponent) observed).addPropertyChangeListener(s, this);
        }
    }

    @Override
    public void propertyChange(@NotNull final PropertyChangeEvent evt) {
        //noinspection unchecked
        notifyEvent(evt.getPropertyName(), (T) evt.getNewValue(), observed);
    }
}
