package edu.kit.mima.api.lambda;

import org.jetbrains.annotations.NotNull;

/**
 * Function with 3 arguments.
 *
 * @author Jannis Weis
 * @since 2018
 */
@FunctionalInterface
public interface TriFunction<T, K, R, S> {

    /**
     * Apply a 3 arguments to the function.
     *
     * @param first  first argument.
     * @param second second argument
     * @param third  third argument.
     * @return return value of function.
     */
    @NotNull S apply(T first, K second, R third);
}
