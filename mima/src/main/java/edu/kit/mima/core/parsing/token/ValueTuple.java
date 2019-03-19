package edu.kit.mima.core.parsing.token;

/**
 * Simple Implementation of {@link ValueTuple}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ValueTuple<T, K> implements Tuple<T, K> {

    private T first;
    private K second;

    /**
     * Create new Value Tuple.
     *
     * @param first  first value
     * @param second second value
     */
    public ValueTuple(final T first, final K second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T getFirst() {
        return first;
    }

    @Override
    public void setFirst(final T first) {
        this.first = first;
    }

    @Override
    public K getSecond() {
        return second;
    }

    @Override
    public void setSecond(final K second) {
        this.second = second;
    }
}
