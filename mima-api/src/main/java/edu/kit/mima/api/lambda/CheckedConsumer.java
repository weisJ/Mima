package edu.kit.mima.api.lambda;

/**
 * Function that may throws an exception.
 *
 * @author Jannis Weis
 * @since 2018
 */

@FunctionalInterface
public interface CheckedConsumer<T, K extends Exception> {

    void accept(T t) throws K;

}
