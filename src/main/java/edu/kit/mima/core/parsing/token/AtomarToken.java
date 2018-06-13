package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class AtomarToken implements Token {

    private final TokenType type;
    private final String value;

    public AtomarToken(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "[type=" + type + "]{ " + value + " }";
    }
}
