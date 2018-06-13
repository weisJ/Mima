package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ObjectToken<T> implements Token {

    private final T value;

    public ObjectToken(T value) {
        this.value = value;
    }


    @Override
    public T getValue() {
        return value;
    }

    @Override
    public TokenType getType() {
        return TokenType.OBJECT;
    }

    @Override
    public String toString() {
        return "[type=object]{" + value.toString() + " }";
    }
}
