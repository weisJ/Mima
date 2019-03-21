package edu.kit.mima.api.observing;

import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract class that classes can extend to implement an class wide observing system. All INSTANCES
 * of this class that may not be needed anymore should call the {@link #close()} method to prevent
 * the object from remaining in memory.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class ClassObservable extends AbstractObservable
        implements Observable, AutoCloseable {

    /**
     * Collection that keeps track of all INSTANCES.
     */
    @NotNull
    protected static final Collection<Observable> INSTANCES = new ArrayList<>();

    public ClassObservable() {
        INSTANCES.add(this);
    }

    /**
     * Add an {@link java.beans.PropertyChangeSupport} that observes all INSTANCES of this class.
     *
     * @param property property to observe
     * @param listener change listener
     */
    public static void addStaticPropertyChangeListener(final String property,
                                                       final PropertyChangeListener listener) {
        for (final var instance : INSTANCES) {
            if (instance != null) {
                instance.addPropertyChangeListener(property, listener);
            }
        }
    }

    /**
     * Add an {@link java.beans.PropertyChangeSupport} that observes all INSTANCES of this class.
     *
     * @param listener change listener
     */
    public static void addStaticPropertyChangeListener(final PropertyChangeListener listener) {
        for (final var instance : INSTANCES) {
            if (instance != null) {
                instance.addPropertyChangeListener(listener);
            }
        }
    }

    /**
     * Remove an {@link java.beans.PropertyChangeSupport} that observes all INSTANCES of this
     * class.
     *
     * @param property observed property
     * @param listener change listener to remove
     */
    public static void removeStaticPropertyChangeListener(final String property,
                                                          final PropertyChangeListener listener) {
        for (final var instance : INSTANCES) {
            if (instance != null) {
                instance.removePropertyChangeListener(property, listener);
            }
        }
    }

    /**
     * Remove an {@link java.beans.PropertyChangeSupport} that observes all INSTANCES of this
     * class.
     *
     * @param listener change listener to remove
     */
    public static void removeStaticPropertyChangeListener(final PropertyChangeListener listener) {
        for (final var instance : INSTANCES) {
            if (instance != null) {
                instance.removePropertyChangeListener(listener);
            }
        }
    }

    /**
     * Close the object.
     */
    public void close() {
        INSTANCES.remove(this);
    }
}
