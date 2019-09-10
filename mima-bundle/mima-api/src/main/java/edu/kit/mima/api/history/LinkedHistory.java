package edu.kit.mima.api.history;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * LinkedHistory containing arbitrary objects.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LinkedHistory<T> implements History<T> {

    protected final HistoryNode headNode;
    protected final HistoryNode tailNode;
    protected HistoryNode currNode;

    protected int maxCapacity;
    protected int head;
    protected int preserved;
    protected int size;

    /**
     * Create LinkedHistory.
     *
     * @param capacity maximum capacity
     */
    @Contract(pure = true)
    public LinkedHistory(final int capacity) {
        headNode = new HistoryNode(null);
        tailNode = new HistoryNode(null);
        currNode = tailNode;

        resetNodes();

        maxCapacity = capacity;
        head = 0;
        preserved = 0;
        size = 0;
    }

    @NotNull
    @Override
    public String toString() {
        var str = new StringBuilder("[");
        var curr = headNode.next;
        int i = 0;
        while (curr != tailNode) {
            if (i == length() - preserved) {
                str.append('|');
            }
            if (i == head) {
                str.append('{');
            }
            str.append(curr.value.toString());
            if (i == head) {
                str.append('}');
            }
            str.append(',');
            curr = curr.next;
            i++;
        }
        str.deleteCharAt(str.length() - 1);
        str.append(']');
        return str.toString();
    }

    @Override
    public void addAtHead(final T element) {
        size -= head;
        headNode.next = currNode;
        currNode.prev = headNode;
        addFirst(element);
        currNode = headNode.next;
        head = 0;
        fireLengthChange();
    }

    @Override
    public void addFront(final T element) {
        addFirst(element);
        currNode = headNode.next;
        head = 0;
        fireLengthChange();
    }


    @Override
    public int length() {
        return size;
    }

    @Override
    public @Nullable T getCurrent() {
        return head < length() ? get(currNode) : null;
    }

    @Override
    public void setCurrent(final T element) {
        currNode.value = element;
    }

    @Override
    public T back() {
        if (head >= length() - preserved) {
            throw new IllegalStateException("Reached end of history");
        }
        final T element = get(currNode);
        currNode = currNode.next;
        head++;
        firePositionChange();
        return element;
    }

    @Override
    public T forward() {
        if (head <= 0) {
            throw new IllegalStateException("Reached front of history");
        }
        final T element = get(currNode.prev);
        currNode = currNode.prev;
        head--;
        firePositionChange();
        return element;
    }

    @Override
    public int capacity() {
        return maxCapacity;
    }

    @Override
    public void reset(final T initial) {
        reset();
        addAtHead(initial);
        preserved = 1;
    }

    @Override
    public void reset() {
        resetNodes();
        size = 0;
        head = 0;
        preserved = 0;
        fireLengthChange();
    }

    private void resetNodes() {
        headNode.prev = tailNode;
        headNode.next = tailNode;
        tailNode.next = headNode;
        tailNode.prev = headNode;
        currNode = tailNode;
    }

    @Override
    public void setCapacity(final int capacity) {
        maxCapacity = capacity;
        while (length() > maxCapacity) {
            removeLast();
        }
        head = Math.max(0, Math.min(head, length() - 1 - preserved));
        if (currNode.next == currNode) {
            currNode = tailNode.prev;
        }
        fireLengthChange();
    }

    @Override
    public int previous() {
        return length() - head - preserved;
    }

    @Override
    public int upcoming() {
        return head;
    }

    @Contract(pure = true)
    private T get(@NotNull final HistoryNode pos) {
        return pos.value;
    }

    private void addFirst(final T value) {
        size++;
        var node = new HistoryNode(value);
        var rightNeighbour = headNode.next;
        rightNeighbour.prev = node;
        node.next = rightNeighbour;
        headNode.next = node;
        node.prev = headNode;
        if (length() > capacity()) {
            removeLast();
        }
    }

    protected void remove(final HistoryNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        if (currNode == node) {
            currNode = currNode.next;
        }
        node.next = node;
        node.prev = node;
        size--;
    }

    protected void removeLast() {
        remove(tailNode.prev);
    }

    protected void fireLengthChange() {
    }

    protected void firePositionChange() {
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            HistoryNode curr = currNode;

            @Override
            public boolean hasNext() {
                return curr != null && curr != tailNode;
            }

            @Override
            public T next() {
                T element = curr.value;
                curr = curr.next;
                return element;
            }
        };
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        for (T item : this) {
            action.accept(item);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    protected final class HistoryNode {

        private HistoryNode prev;
        private HistoryNode next;

        private T value;

        @Contract(pure = true)
        private HistoryNode(final T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}
