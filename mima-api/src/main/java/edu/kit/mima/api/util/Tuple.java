package edu.kit.mima.api.util;

/**
 * Tuple class with two values.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Tuple<T, K> {

    /**
     * Get the first value.
     *
     * @return first value
     */
    T getFirst();

    /**
     * Set first value.
     *
     * @param first first value
     */
    void setFirst(T first);

    /**
     * Get the second value.
     *
     * @return second value
     */
    K getSecond();

    /**
     * Set second value.
     *
     * @param second second value
     */
    void setSecond(K second);
}
