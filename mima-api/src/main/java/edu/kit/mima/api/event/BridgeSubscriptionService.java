package edu.kit.mima.api.event;

import edu.kit.mima.api.observing.ClassObservable;
import edu.kit.mima.api.observing.Observable;
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
    private final Type type;

    public <K extends Observable> BridgeSubscriptionService(@NotNull final K observed) {
        super(observed.getClass());
        this.observed = observed;
        this.type = Type.OBSERVABLE;
    }

    public <K extends ClassObservable> BridgeSubscriptionService(@NotNull final K observed) {
        super(observed.getClass());
        this.observed = observed;
        this.type = Type.CLASS_OBSERVABLE;

    }

    public <K extends JComponent> BridgeSubscriptionService(@NotNull final K observed) {
        super(observed.getClass());
        this.observed = observed;
        this.type = Type.COMPONENT;
    }


    @Override
    void initService(@NotNull final String... identifiers) {
        for (var s : identifiers) {
            switch (type) {
                case OBSERVABLE -> ((Observable) observed).addPropertyChangeListener(s, this);
                case CLASS_OBSERVABLE -> ((ClassObservable) observed).addPropertyChangeListener(s,
                        this);
                case COMPONENT -> ((JComponent) observed).addPropertyChangeListener(s, this);
            }
        }
    }

    @Override
    public void propertyChange(@NotNull final PropertyChangeEvent evt) {
        //noinspection unchecked
        notifyEvent(evt.getPropertyName(), (T) evt.getNewValue(), observed);
    }

    private enum Type {
        OBSERVABLE,
        CLASS_OBSERVABLE,
        COMPONENT,
    }
}
