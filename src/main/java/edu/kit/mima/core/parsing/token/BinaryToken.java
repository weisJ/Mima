package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> implements Token {

    public static final Token EMPTY_TOKEN = new Token() {
        @Override
        public Object getValue() {
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

    public BinaryToken(TokenType type, T first, K second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public K getSecond() {
        return second;
    }

    @Override
    public T getValue() {
        return null;
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
