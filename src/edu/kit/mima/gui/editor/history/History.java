package edu.kit.mima.gui.editor.history;

import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class History<T> {

    private final LinkedList<T> history;
    private final int maxCapacity;
    private int top;

    public History(int capacity) {
        history = new LinkedList<>();
        maxCapacity = capacity;
        top = 0;
    }

    public String toString() {
        return history.toString() + "[" + top + "]";
    }

    public void add(T element) {
        int removeCount = top;
        for (int i = 0; i < removeCount; i++) {
            history.removeFirst();
            top--;
        }
        history.addFirst(element);
        if (history.size() > maxCapacity) {
            history.removeLast();
        }
    }

    public int legth() {
        return history.size();
    }

    public void setCurrent(T element) {
        history.set(top, element);
    }

    public T getCurrent() {
        return history.get(top);
    }

    public T back() {
        try {
            top++;
            return history.get(top);
        } catch (IndexOutOfBoundsException e) {
            top--;
            return null;
        }
    }

    public T forward() {
        try {
            top--;
            if (top < 0) top = 0;
            return history.get(--top);
        } catch (IndexOutOfBoundsException e) {
            top++;
            return null;
        }
    }

    public int capacity() {
        return capacity();
    }

    public void reset() {
        top = 0;
        while (!history.isEmpty()) {
            history.removeFirst();
        }
    }
}
