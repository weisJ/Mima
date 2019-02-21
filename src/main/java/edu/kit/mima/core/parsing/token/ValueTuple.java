package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ValueTuple<T, K> implements Tuple<T, K> {

    private T first;
    private K second;

    public ValueTuple(T first, K second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T getFirst() {
        return first;
    }

    @Override
    public void setFirst(T first) {
        this.first = first;
    }

    @Override
    public K getSecond() {
        return second;
    }

    @Override
    public void setSecond(K second) {
        this.second = second;
    }
}
