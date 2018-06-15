package edu.kit.mima.core.parsing.token;

/**
 * Implementation of a {@link Token} that holds a single value
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token<T> {

    private final TokenType type;
    private final T value;

    /**
     * Plain Token that holds any value type
     *
     * @param type type of token
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
    public String toString() {
        return "[type=" + type + "]{ " + value + " }";
    }
}
