package edu.kit.mima.api.util;

/**
 * Immutable implementation for {@link Tuple}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface ImmutableTuple<T, K> extends Tuple<T, K> {

    /**
     * Not supported. Will throw UnsupportedOperationException
     *
     * @param firstValue firstValue
     */
    @Override
    default void setFirst(final T firstValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. Will throw UnsupportedOperationException
     *
     * @param secondValue secondValue
     */
    @Override
    default void setSecond(final K secondValue) {
        throw new UnsupportedOperationException();
    }
}
