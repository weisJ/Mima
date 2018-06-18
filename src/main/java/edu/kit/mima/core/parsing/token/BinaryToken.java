package edu.kit.mima.core.parsing.token;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Implementation of a {@link Token} that holds two values
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> implements Token<T>, Tuple<T, K> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    private final TokenType type;
    private T first;
    private K second;

    /**
     * Token that holds two values independent of each others type
     *
     * @param type   type of the token
     * @param first  first value
     * @param second second value
     */
    public BinaryToken(TokenType type, T first, K second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    /**
     * Get the first value. Is equivalent to getValue()
     *
     * @return first value
     */
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

    @Override
    public T getValue() {
        return first;
    }

    @Override
    public void setValue(T value) {
        first = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type=" + type + "] {\n\t"
                + INDENT.matcher(first.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n\t"
                + INDENT.matcher(second.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n}";
    }

    @Override
    public String simpleName() {
        return first.toString() + second.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BinaryToken<?, ?> that = (BinaryToken<?, ?>) obj;
        return type == that.type
                && Objects.equals(first, that.first)
                && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, first, second);
    }
}
