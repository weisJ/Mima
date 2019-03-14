package edu.kit.mima.core.parsing.token;

/**
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
    default void setFirst(T firstValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. Will throw UnsupportedOperationException
     *
     * @param secondValue secondValue
     */
    @Override
    default void setSecond(K secondValue) {
        throw new UnsupportedOperationException();
    }
}
