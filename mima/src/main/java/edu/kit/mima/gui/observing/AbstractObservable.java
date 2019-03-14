package edu.kit.mima.gui.observing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Jannis Weis
 * @since 2018
 */
public abstract class AbstractObservable implements Observable {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
