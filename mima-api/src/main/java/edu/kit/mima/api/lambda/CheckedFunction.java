package edu.kit.mima.api.lambda;

import org.jetbrains.annotations.NotNull;

/**
 * Function that may throws an exception.
 *
 * @author Jannis Weis
 * @since 2018
 */

@FunctionalInterface
public interface CheckedFunction<T, R> {

    @NotNull R apply(T t) throws Exception;

}
