package edu.kit.mima.api.history;

import org.jetbrains.annotations.Nullable;

/**
 * History interface.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface History<T> extends Iterable<T> {

    String LENGTH_PROPERTY = "historyLength";
    String POSITION_PROPERTY = "historyPosition";


    /**
     * Add to history. If history reaches maximum capacity oldest object will be deleted. If there are
     * elements in front of the current one they too will be deleted.
     *
     * @param element element to addAtHead
     */
    void addAtHead(final T element);

    /**
     * Add element to the front of the history.
     * Moves the head of the history to the added element.
     *
     * @param element element to addAtHead.
     */
    void addFront(final T element);

    /**
     * Get the length of the history.
     *
     * @return length of history
     */
    int length();

    /**
     * Get the current object in history.
     *
     * @return current object
     */
    @Nullable T getCurrent();

    /**
     * Overwrite the current object.
     *
     * @param element element to put at current position
     */
    void setCurrent(final T element);

    /**
     * Go back in history.
     *
     * @return element at new history position
     */
    T back();

    /**
     * Go forward in history.
     *
     * @return element at new history position
     */
    T forward();

    /**
     * Get the maximum capacity of this history.
     *
     * @return capacity
     */
    int capacity();

    /**
     * Reset the history.
     *
     * @param initial the initial object.
     */
    void reset(final T initial);

    /**
     * Reset the history.
     */
    void reset();

    /**
     * Set the capacity for the history.
     *
     * @param capacity new capacity
     */
    void setCapacity(final int capacity);

    /**
     * Get amount of previous states.
     *
     * @return number of previous states.
     */
    int previous();

    /**
     * Get amount of upcoming states.
     *
     * @return number of upcoming states.
     */
    int upcoming();
}
