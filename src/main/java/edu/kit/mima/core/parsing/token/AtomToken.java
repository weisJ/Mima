package edu.kit.mima.core.parsing.token;

import java.util.Objects;

/**
 * Implementation of a {@link Token} that holds a single value
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token<T> {

    private final TokenType type;
    private T value;

    /**
     * Plain Token that holds any value type
     *
     * @param type  type of token
     * @param value value of token
     */
    public AtomToken(TokenType type, T value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[type=" + type + "]{ " + value + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomToken<?> atomToken = (AtomToken<?>) o;
        return type == atomToken.type
                && Objects.equals(value, atomToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
