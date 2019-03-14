package edu.kit.mima.gui.observing;

import java.beans.PropertyChangeListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Observable {

    void addPropertyChangeListener(String property, PropertyChangeListener listener);

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(String property, PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);
}
