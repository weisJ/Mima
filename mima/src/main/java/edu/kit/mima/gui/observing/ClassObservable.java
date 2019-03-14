package edu.kit.mima.gui.observing;

import java.beans.PropertyChangeListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface ClassObservable<T> {

    void addStaticPropertyChangeListener(String property, PropertyChangeListener listener);

    void addStaticPropertyChangeListener(PropertyChangeListener listener);

    void removeStaticPropertyChangeListener(String property, PropertyChangeListener listener);

    void removeStaticPropertyChangeListener(PropertyChangeListener listener);
}
