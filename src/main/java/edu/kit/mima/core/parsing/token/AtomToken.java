package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;

import java.util.Objects;

/**
 * Implementation of a {@link Token} that holds a single value
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token<T> {

    private final TokenType type;
    private int index;
    private T value;

    /**
     * Plain Token that holds any value type
     *
     * @param type  type of token
     * @param value value of token
     * @param index index of token
     */
    public AtomToken(TokenType type, T value, int index) {
        this.type = type;
        this.value = value;
        this.index = index;
    }

    /**
     * Plain Token that holds any value type
     *
     * @param type  type of token
     * @param value value of token
     */
    public AtomToken(TokenType type, T value) {
        this(type, value, -1);
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public int getIndexAttribute() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
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
    public String simpleName() {
        StringBuilder prefix = new StringBuilder();
        if (type == TokenType.DEFINITION) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ');
        } else if (type == TokenType.CONSTANT) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ')
                    .append(Keyword.CONSTANT).append(' ');
        } else if (type == TokenType.BINARY) {
            prefix.append('~');
        }
        if (value instanceof ArrayToken && !(prefix.length() == 0)) {
            String name = ((ArrayToken) value).simpleName();
            return prefix + name.substring(1, name.length() - 1);
        } else if (value instanceof Token) {
            return prefix + ((Token) value).simpleName();
        } else {
            return prefix + value.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AtomToken<?> atomToken = (AtomToken<?>) obj;
        return type == atomToken.type
                && Objects.equals(value, atomToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
