package edu.kit.mima.gui.components.editor.history;

import edu.kit.mima.gui.observing.AbstractObservable;
import edu.kit.mima.gui.observing.ClassObservable;
import edu.kit.mima.gui.observing.SharedInstance;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

/**
 * History containing arbitrary objects
 *
 * @author Jannis Weis
 * @since 2018
 */
public class History<T> extends AbstractObservable implements ClassObservable {

    public static final String LENGTH_PROPERTY = "historyLength";
    public static final String POSITION_PROPERTY = "historyPosition";
    private static final PropertyChangeSupport CHANGE_SUPPORT = new PropertyChangeSupport(ClassObservable.class);
    @SharedInstance
    private static final History instance = new History(0);
    private final LinkedList<T> history;
    private int maxCapacity;
    private int head;

    /**
     * Create History
     *
     * @param capacity maximum capacity
     */
    public History(final int capacity) {
        super();
        history = new LinkedList<>();
        maxCapacity = capacity;
        head = 0;
        firePositionChange(head);
        fireLengthChange(0);
    }

    /**
     * String representation of History
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return history.toString() + '[' + head + ']';
    }

    /**
     * Add to history. If history reaches maximum capacity oldest object will be deleted
     * If there are elements in front of the current one they too will be deleted
     *
     * @param element element to add
     */
    public void add(final T element) {
        int prevSize = length();
        final int removeCount = head;
        for (int i = 0; i < removeCount; i++) {
            history.removeFirst();
            head--;
        }
        history.addFirst(element);
        if (history.size() > maxCapacity) {
            history.removeLast();
        }
        fireLengthChange(prevSize);
    }

    /**
     * Get the length of the history
     *
     * @return length of history
     */
    public int length() {
        return history.size();
    }

    /**
     * Get the current object in history
     *
     * @return current object
     */
    public @Nullable T getCurrent() {
        return (head < history.size()) ? history.get(head) : null;
    }

    /**
     * overwrite the current object.
     *
     * @param element element to put at current position
     */
    public void setCurrent(final T element) {
        history.set(head, element);
    }

    /**
     * Go back in history
     *
     * @return element at new history position
     */
    public T back() throws IndexOutOfBoundsException {
        assert head < length() : "reached end of history";
        T element = history.get(head);
        head++;
        firePositionChange(head - 1);
        return element;
    }

    /**
     * Go forward in history
     *
     * @return element at new history position
     */
    public T forward() throws IndexOutOfBoundsException {
        assert head != 0 : "already on newest version";
        T element = history.get(head - 1);
        head--;
        firePositionChange(head + 1);
        return element;
    }

    /**
     * Get the maximum capacity of this history
     *
     * @return capacity
     */
    public int capacity() {
        return maxCapacity;
    }

    /**
     * reset the history
     */
    public void reset() {
        int prevSize = length();
        head = 0;
        while (!history.isEmpty()) {
            history.removeFirst();
        }
        fireLengthChange(prevSize);
    }

    /**
     * Set the capacity for the history
     *
     * @param capacity new capacity
     */
    public void setCapacity(final int capacity) {
        int prevSize = length();
        maxCapacity = capacity;
        while (history.size() > maxCapacity) {
            history.removeLast();
        }
        head = Math.max(0, Math.min(head, history.size() - 1));
        fireLengthChange(prevSize);
    }

    /**
     * Get amount of previous states.
     *
     * @return number of previous states.
     */
    public int previous() {
        return length() - head;
    }

    /**
     * Get amount of upcoming states.
     *
     * @return number of upcoming states.
     */
    public int upcoming() {
        return head;
    }

    private void fireLengthChange(int prevSize) {
        getPropertyChangeSupport().firePropertyChange(LENGTH_PROPERTY, prevSize, length());
        CHANGE_SUPPORT.firePropertyChange(LENGTH_PROPERTY, prevSize, length());
    }

    private void firePositionChange(int prevPos) {
        getPropertyChangeSupport().firePropertyChange(POSITION_PROPERTY, prevPos, head);
        CHANGE_SUPPORT.firePropertyChange(POSITION_PROPERTY, prevPos, head);
    }

    @Override
    public void addStaticPropertyChangeListener(String property, PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(property, listener);
    }

    @Override
    public void addStaticPropertyChangeListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }

    @Override
    public void removeStaticPropertyChangeListener(String property, PropertyChangeListener listener) {
        CHANGE_SUPPORT.removePropertyChangeListener(property, listener);
    }

    @Override
    public void removeStaticPropertyChangeListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }

}
