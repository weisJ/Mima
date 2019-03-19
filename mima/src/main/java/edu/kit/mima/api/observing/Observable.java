package edu.kit.mima.api.observing;

import java.beans.PropertyChangeListener;

/**
 * Interface for Objects that can be observed through {@link PropertyChangeListener}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Observable {

    void addPropertyChangeListener(String property, PropertyChangeListener listener);

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(String property, PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);
}
