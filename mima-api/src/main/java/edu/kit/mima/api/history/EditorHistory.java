package edu.kit.mima.api.history;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class EditorHistory extends SubscribableHistory<FileHistoryObject> {

    /**
     * Create LinkedHistory.
     *
     * @param capacity maximum capacity
     */
    public EditorHistory(final int capacity) {
        super(capacity);
    }
}
