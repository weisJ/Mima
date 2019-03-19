package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Utility class that provides functions to help with Lambda creation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class LambdaUtil {

    @Contract(" -> fail")
    private LambdaUtil() {
        assert false : "utility class Constructor";
    }

    /**
     * Create lambda of type {@link BiConsumer} that can recursively call itself.
     *
     * @param f   Function that takes a function and returns the lambda
     * @param <T> First type parameter of {@link BiConsumer}
     * @param <K> Second type parameter of {@link BiConsumer}
     * @return recursive lambda.
     */
    @NotNull
    @Contract(pure = true)
    public static <T, K> BiConsumer<T, K> createRecursive(
            @NotNull final Function<BiConsumer<T, K>, BiConsumer<T, K>> f) {
        return (t, k) -> f.apply(createRecursive(f)).accept(t, k);
    }
}
