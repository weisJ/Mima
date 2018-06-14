package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token {

    private final TokenType type;
    private final T value;

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
        return "[type=" + type + "]{ " + value.toString() + " }";
    }
}
