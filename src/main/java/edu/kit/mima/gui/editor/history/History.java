package edu.kit.mima.gui.editor.history;

import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class History<T> {

    private final LinkedList<T> history;
    private final int maxCapacity;
    private int head;

    /**
     * Create History
     *
     * @param capacity maximum capacity
     */
    public History(final int capacity) {
        history = new LinkedList<>();
        maxCapacity = capacity;
        head = 0;
    }

    /**
     * String representation of History
     *
     * @return string representation
     */
    public String toString() {
        return history.toString() + "[" + head + "]";
    }

    /**
     * Add to history. If history reaches maximum capacity oldest object will be deleted
     * If there are elements in front of the current one they too will be deleted
     * @param element element to add
     */
    public void add(T element) {
        int removeCount = head;
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
    public T getCurrent() {
        return history.get(head);
    }

    /**
     * overwrite the current object.
     *
     * @param element element to put at current position
     */
    public void setCurrent(T element) {
        history.set(head, element);
    }

    /**
     * Go back in history
     *
     * @return element at new histoy position
     */
    public T back() {
        if (head >= length() - 1) {
            throw new IndexOutOfBoundsException("reached end of history");
        }
        head++;
        return history.get(head);
    }

    /**
     * Go forward in history
     *
     * @return element at new histoy position
     */
    public T forward() {
        if (head == 0) {
            throw new IndexOutOfBoundsException("already on newest version");
        }
        head--;
        return history.get(head);
    }

    /**
     * Get the maximum capacity of this history
     *
     * @return capacity
     */
    public int capacity() {
        return capacity();
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
}
