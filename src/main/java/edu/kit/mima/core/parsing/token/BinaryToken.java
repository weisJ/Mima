package edu.kit.mima.core.parsing.token;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Implementation of a {@link Token} that holds two values
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> implements Token<T>, Tuple<T, K> {

    /**
     * Empty token to use with binary tokens that should explicitly not have any second value
     */
    public static final Token EMPTY_TOKEN = new Token() {
        @Override
        public @Nullable Object getValue() {
            return null;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public TokenType getType() {
            return TokenType.EMPTY;
        }

        @Override
        public String toString() {
            return "[type=EMPTY] { }";
        }
    };

    private TokenType type;
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
        this.first = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    /**
     * Set the type of this binary token
     *
     * @param type type
     */
    public void setType(TokenType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "[type=" + type + "] {\n\t"
                + first.toString().replaceAll("\n", "\n\t")
                + "\n\t"
                + second.toString().replaceAll("\n", "\n\t")
                + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryToken<?, ?> that = (BinaryToken<?, ?>) o;
        return type == that.type
                && Objects.equals(first, that.first)
                && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, first, second);
    }
}
