package edu.kit.mima.api.lambda;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
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


    /**
     * Wrap checked function to normal function.
     *
     * @param checkedFunction checked function to wrap.
     * @param <T>             First type argument of function.
     * @param <R>             Second type argument of function.
     * @return wrapped function.
     */
    @NotNull
    @Contract(pure = true)
    public static <T, R> Function<T, R> wrap(@NotNull final CheckedFunction<T, R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Wrap checked consumer to normal function.
     *
     * @param checkedFunction checked consumer to wrap.
     * @param <T>             type argument of consumer.
     * @return wrapped function.
     */
    @NotNull
    @Contract(pure = true)
    public static <T> Consumer<T> wrap(@NotNull final CheckedConsumer<T> checkedFunction) {
        return t -> {
            try {
                checkedFunction.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Wrap checked consumer to normal function.
     *
     * @param checkedFunction checked consumer to wrap.
     * @param <T>             first type argument of consumer.
     * @param <K>             second type argument of consumer.
     * @return wrapped function.
     */
    @NotNull
    @Contract(pure = true)
    public static <T, K> BiConsumer<T, K> wrap(
            @NotNull final CheckedBiConsumer<T, K> checkedFunction) {
        return (t, k) -> {
            try {
                checkedFunction.accept(t, k);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @NotNull
    @Contract(pure = true)
    public static <T, K> Consumer<K> reduceFirst(@NotNull final BiConsumer<T, K> cons,
                                                 final T first) {
        return (K k) -> cons.accept(first, k);
    }

    @NotNull
    @Contract(pure = true)
    public static <T, K> Consumer<T> reduceSecond(@NotNull final BiConsumer<T, K> cons,
                                                  final K second) {
        return (T t) -> cons.accept(t, second);
    }

    @NotNull
    @Contract(pure = true)
    public static <T, K> BiConsumer<T, K> liftFirst(@NotNull final Consumer<K> cons) {
        return (T t, K k) -> cons.accept(k);
    }

    @NotNull
    @Contract(pure = true)
    public static <T, K> BiConsumer<T, K> liftSecond(@NotNull final Consumer<T> cons) {
        return (T t, K k) -> cons.accept(t);
    }


}
