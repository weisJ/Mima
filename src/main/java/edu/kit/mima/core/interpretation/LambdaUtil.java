package edu.kit.mima.core.interpretation;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class LambdaUtil {

    private LambdaUtil() {
        assert false : "utility class Constructor";
    }

    /**
     * Create lambda of type {@link BiConsumer<>} that can recursively call itself
     *
     * @param f   Function that takes a function and returns the lambda
     * @param <T> First type parameter of {@link BiConsumer<>}
     * @param <K> Second type parameter of {@link BiConsumer<>}
     * @return recursive lambda.
     */
    public static <T, K> BiConsumer<T, K> createRecursive(Function<BiConsumer<T, K>, BiConsumer<T, K>> f) {
        return (env, i) -> f.apply(createRecursive(f)).accept(env, i);
    }
}
