package edu.kit.mima.core.data;

/**
 * Memory interface
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Memory<T> {

    /**
     * Load value from memory
     *
     * @param index index in memory
     * @return memory at index
     */
    T loadValue(int index);

    /**
     * Store value to memory
     *
     * @param index index to store at
     * @param value value to store
     */
    void storeValue(int index, T value);

    /**
     * Reset the memory. Changes all saved values to original state
     */
    void reset();
}
