package edu.kit.mima.core.interpretation;

/**
 * Value that has a {@link ValueType} and holds value.
 *
 * @param <T> Type of value.
 * @author Jannis Weis
 * @since 2018
 */
public class Value<T> {

    private final ValueType type;
    private final T value;

    /**
     * create Value with given type and content.
     *
     * @param type  type of value
     * @param value value content
     */
    public Value(final ValueType type, final T value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Get the value content.
     *
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the value type.
     *
     * @return value type
     */
    public ValueType getType() {
        return type;
    }
}
