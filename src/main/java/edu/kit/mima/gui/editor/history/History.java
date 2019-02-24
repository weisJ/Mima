package edu.kit.mima.gui.editor.history;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * History containing arbitrary objects
 *
 * @author Jannis Weis
 * @since 2018
 */
public class History<T> {

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
        final int removeCount = head;
        for (int i = 0; i < removeCount; i++) {
            history.removeFirst();
            head--;
        }
        history.addFirst(element);
        if (history.size() > maxCapacity) {
            history.removeLast();
        }
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
        head = 0;
        while (!history.isEmpty()) {
            history.removeFirst();
        }
    }

    /**
     * Set the capacity for the history
     *
     * @param capacity new capacity
     */
    public void setCapacity(final int capacity) {
        assert capacity >= maxCapacity : "Can't crop history. Too small capacity";
        maxCapacity = capacity;
    }
}
