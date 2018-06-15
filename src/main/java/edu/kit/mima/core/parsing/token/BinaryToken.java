package edu.kit.mima.core.parsing.token;

import org.jetbrains.annotations.Nullable;

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
        public TokenType getType() {
            return TokenType.EMPTY;
        }

        @Override
        public String toString() {
            return "[type=EMPTY] { }";
        }
    };

    private final TokenType type;
    private final T first;
    private final K second;

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
    public K getSecond() {
        return second;
    }

    @Override
    public T getValue() {
        return first;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type=" + type + "] {\n\t"
                + first.toString().replaceAll("\n", "\n\t")
                + "\n\t"
                + second.toString().replaceAll("\n", "\n\t")
                + "\n}";
    }
}
