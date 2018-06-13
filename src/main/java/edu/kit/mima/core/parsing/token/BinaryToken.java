package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken implements Token {

    private static final Token EMPTY_TOKEN = new Token() {
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
    private final Token first;
    private final Token second;

    public BinaryToken(TokenType type, Token first, Token second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public BinaryToken(TokenType type, Token first) {
        this(type, first, EMPTY_TOKEN);
    }

    public Token getFirst() {
        return first;
    }

    public Token getSecond() {
        return second;
    }

    @Override
    public Token getValue() {
        return null;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type=" + type + "] {" + "\n\t"
                + first.toString().replaceAll("\n", "\n\t")
                + "\n\t"
                + second.toString().replaceAll("\n", "\n\t")
                + "\n}";
    }
}
