package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Tuple<T, K> {

    /**
     * Get the first value
     *
     * @return first value
     */
    T getFirst();

    /**
     * Get the second value
     *
     * @return second value
     */
    K getSecond();
}
