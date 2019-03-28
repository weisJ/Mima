package edu.kit.mima.api.lambda;

/**
 * Function that may throws an exception.
 *
 * @author Jannis Weis
 * @since 2018
 */

@FunctionalInterface
public interface CheckedBiConsumer<T, K> {

    void accept(T t, K k) throws Exception;
}
