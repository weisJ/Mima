package edu.kit.mima.api.history;

import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SubscriptionService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LinkedHistory containing arbitrary objects.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LinkedHistory<T> implements History<T> {

    private static final SubscriptionService<Integer> SUBSCRIPTION_SERVICE = new SubscriptionService<>();

    static {
        SubscriptionManager.getCurrentManager().offerSubscription(SUBSCRIPTION_SERVICE,
                                                                  LENGTH_PROPERTY,
                                                                  POSITION_PROPERTY);
    }

    private final HistoryNode headNode;
    private final HistoryNode tailNode;
    private HistoryNode currNode;

    private int maxCapacity;
    private int head;
    private int preserved;
    private int size;

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

    private void removeLast() {
        var removedNode = tailNode.prev;
        var node = tailNode.prev.prev;
        tailNode.prev = node;
        node.next = tailNode;

        removedNode.next = removedNode;
        removedNode.prev = removedNode;
        size--;
    }

    private void fireLengthChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(LENGTH_PROPERTY, length(), this);
    }

    private void firePositionChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(POSITION_PROPERTY, head, this);
    }

    private final class HistoryNode {

        private HistoryNode prev;
        private HistoryNode next;

        private T value;

        @Contract(pure = true)
        private HistoryNode(final T value) {
            this.value = value;
        }
    }
}
