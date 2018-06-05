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

    public History(int capacity) {
        history = new LinkedList<>();
        maxCapacity = capacity;
        head = 0;
    }

    public String toString() {
        return history.toString() + "[" + head + "]";
    }

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

    public int length() {
        return history.size();
    }

    public T getCurrent() {
        return history.get(head);
    }

    public void setCurrent(T element) {
        history.set(head, element);
    }

    public T back() {
        if (head >= length() - 1) {
            throw new IndexOutOfBoundsException("reached end of history");
        }
        head++;
        return history.get(head);
    }

    public T forward() {
        if (head == 0) {
            throw new IndexOutOfBoundsException("already on newest version");
        }
        head--;
        return history.get(head);
    }

    public int capacity() {
        return capacity();
    }

    public void reset() {
        head = 0;
        while (!history.isEmpty()) {
            history.removeFirst();
        }
    }
}
