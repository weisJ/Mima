package edu.kit.mima.core.interpretation;

import edu.kit.mima.api.util.ValueTuple;

/**
 * Value that has a {@link ValueType} and holds value.
 *
 * @param <T> Type of value.
 * @author Jannis Weis
 * @since 2018
 */
public class Value<T> extends ValueTuple<T, ValueType> {

    /**
     * create Value with given type and content.
     *
     * @param type  type of value
     * @param value value content
     */
    public Value(final ValueType type, final T value) {
        super(value, type);
    }

    /**
     * Get the value content.
     *
     * @return value
     */
    public T getValue() {
        return getFirst();
    }

    /**
     * Get the value type.
     *
     * @return value type
     */
    public ValueType getType() {
        return getSecond();
    }
}
