package edu.kit.mima.core.parsing.lang;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Punctuation {

    public static final char DEFINITION_BEGIN = '§';
    public static final char DEFINITION_DELIMITER = ':';
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSED_BRACKET = ')';
    public static final char INSTRUCTION_END = ';';
    public static final char BINARY_PREFIX = '~';
    public static final char COMMENT = '#';
    public static final char COMMA = ',';

    private Punctuation() {
        assert false : "utility constructor";
    }

    public static char[] getPunctuation() {
        return new char[]{
                DEFINITION_BEGIN,
                DEFINITION_DELIMITER,
                OPEN_BRACKET,
                CLOSED_BRACKET,
                INSTRUCTION_END,
                BINARY_PREFIX,
                COMMENT,
                COMMA,
        };
    }
}
