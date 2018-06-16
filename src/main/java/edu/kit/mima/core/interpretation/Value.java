package edu.kit.mima.core.interpretation;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Value<T> {

    private final ValueType type;
    private final T value;

    public Value(ValueType type, T value) {
        this.type = type;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public ValueType getType() {
        return type;
    }
}
