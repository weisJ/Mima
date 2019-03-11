package edu.kit.mima.gui.util;

import java.util.ArrayList;
import java.util.List;

/**
 * IndexFactory for creating Ids in the integer range
 * each created index will be unique
 *
 * @author Jannis Weis
 */
public final class IndexFactory {

    private final int lowerBound;
    private final int upperBound;
    private final List<Integer> used;

    /**
     * Create new IndexFactory that supports values between
     * lowerBound and upperBound for the ids
     * must conform 0 <= lower <= upperBound
     *
     * @param lowerBound lowerBound for index values
     * @param upperBound upperBound for index values
     */
    public IndexFactory(int lowerBound, int upperBound) {
        if (lowerBound < 0 || upperBound < 0 || lowerBound > upperBound) {
            throw new IllegalArgumentException("Invalid bound specifications");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.used = new ArrayList<>();
    }

    /**
     * Create new IndexFactory with only lowerBound. upperBound is Integer.MAX_VALUE
     * must conform lowerBound >= 0
     *
     * @param lowerBound lowerBound for index values
     */
    public IndexFactory(int lowerBound) {
        this(lowerBound, Integer.MAX_VALUE);
    }


    /**
     * Get the index which has the smallest value bigger tha lowerBound that isn't used yet.
     * Is unavailable for further use afterwards
     *
     * @return index with next free value
     */
    public int nextIndex() {
        int nextIndex = peekNextIndex();
        used.add(nextIndex);
        return nextIndex;
    }

    /**
     * Get an index with the smallest value bigger tha lowerBound that isn't used yet.
     * Can be used until it is setUsed()
     *
     * @return index with next free value
     */
    public int peekNextIndex() {
        if (used.size() == upperBound - lowerBound) {
            throw new IllegalStateException("No more valid ids available");
        }
        for (int i = lowerBound; i < upperBound; i++) {
            if (!used.contains(i)) {
                return i;
            }
        }
        return -1; //Is never executed, as the exception gets thrown before
    }

    /**
     * Reset the idFactory. Afterwards all index values can be used again
     */
    public void reset() {
        used.clear();
    }

    /**
     * Set a given index value free, so it can be used again
     *
     * @param index index value
     */
    public void setFree(int index) {
        used.remove(index);
    }

    /**
     * Make an given index unavailable for further use until it is set free again
     *
     * @param index index to make unavailable
     */
    public void setUsed(int index) {
        used.add(index);
    }

    /**
     * Returns whether a given value is already used for an id
     *
     * @param index index value
     * @return true if there exists an index produced by this factory with value = index
     */
    private boolean isUsed(int index) {
        return used.contains(index);
    }
}
