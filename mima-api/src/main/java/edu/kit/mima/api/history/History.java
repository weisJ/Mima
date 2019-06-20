package edu.kit.mima.api.history;

import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SubscriptionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * History containing arbitrary objects.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class History<T> {

    public static final String LENGTH_PROPERTY = "historyLength";
    public static final String POSITION_PROPERTY = "historyPosition";
    private static final SubscriptionService<Integer> SUBSCRIPTION_SERVICE =
            new SubscriptionService<>();

    static {
        SubscriptionManager.getCurrentManager()
                .offerSubscription(SUBSCRIPTION_SERVICE, LENGTH_PROPERTY, POSITION_PROPERTY);
    }

    @NotNull
    private final LinkedList<T> history;
    private int maxCapacity;
    private int head;
    private int preserved;

    /**
     * Create History.
     *
     * @param capacity maximum capacity
     */
    public History(final int capacity) {
        super();
        history = new LinkedList<>();
        maxCapacity = capacity;
        head = 0;
        preserved = 0;
    }

    /**
     * String representation of History.
     *
     * @return string representation
     */
    @NotNull
    @Override
    public String toString() {
        return history.toString() + '[' + head + ']';
    }

    /**
     * Add to history. If history reaches maximum capacity oldest object will be deleted. If there are
     * elements in front of the current one they too will be deleted.
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
        fireLengthChange();
    }

    /**
     * Add element to the front of the history.
     * Moves the head of the history to the added element.
     *
     * @param element element to add.
     */
    public void addFront(final T element) {
        history.addFirst(element);
        head = 0;
        fireLengthChange();
    }

    /**
     * Get the length of the history.
     *
     * @return length of history
     */
    public int length() {
        return history.size();
    }

    /**
     * Get the current object in history.
     *
     * @return current object
     */
    public @Nullable T getCurrent() {
        return head < history.size() ? history.get(head) : null;
    }

    /**
     * Overwrite the current object.
     *
     * @param element element to put at current position
     */
    public void setCurrent(final T element) {
        history.set(head, element);
    }

    /**
     * Go back in history.
     *
     * @return element at new history position
     */
    public T back() {
        assert head < length() : "reached end of history";
        final T element = history.get(head);
        head++;
        firePositionChange();
        return element;
    }

    /**
     * Go forward in history.
     *
     * @return element at new history position
     */
    public T forward() {
        assert head != 0 : "already on newest version";
        final T element = history.get(head - 1);
        head--;
        firePositionChange();
        return element;
    }

    /**
     * Get the maximum capacity of this history.
     *
     * @return capacity
     */
    public int capacity() {
        return maxCapacity;
    }

    /**
     * Reset the history.
     *
     * @param initial the initial object.
     */
    public void reset(final T initial) {
        reset();
        add(initial);
        preserved = 1;
    }

    /**
     * Reset the history.
     */
    public void reset() {
        head = 0;
        history.clear();
        preserved = 0;
        fireLengthChange();
    }

    /**
     * Set the capacity for the history.
     *
     * @param capacity new capacity
     */
    public void setCapacity(final int capacity) {
        maxCapacity = capacity;
        while (history.size() > maxCapacity) {
            history.removeLast();
        }
        head = Math.max(0, Math.min(head, history.size() - 1));
        fireLengthChange();
    }

    /**
     * Get amount of previous states.
     *
     * @return number of previous states.
     */
    public int previous() {
        return length() - head - preserved;
    }

    /**
     * Get amount of upcoming states.
     *
     * @return number of upcoming states.
     */
    public int upcoming() {
        return head;
    }

    private void fireLengthChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(LENGTH_PROPERTY, length(), this);
    }

    private void firePositionChange() {
        SUBSCRIPTION_SERVICE.notifyEvent(POSITION_PROPERTY, head, this);
    }
}
