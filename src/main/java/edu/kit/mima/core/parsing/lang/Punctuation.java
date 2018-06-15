package edu.kit.mima.core.parsing.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

/**
 * Punctuation used in {@link Parser} and {@link TokenStream}
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Punctuation {

    /**
     * Begin of definition
     */
    public static final char DEFINITION_BEGIN = '§';
    /**
     * definition separator/delimiter
     */
    public static final char DEFINITION_DELIMITER = ':';
    /**
     * Open bracket
     */
    public static final char OPEN_BRACKET = '(';
    /**
     * Closed bracket
     */
    public static final char CLOSED_BRACKET = ')';
    /**
     * End of line/instruction
     */
    public static final char INSTRUCTION_END = ';';
    /**
     * Prefix for binary numbers
     */
    public static final char BINARY_PREFIX = '~';
    /**
     * prefix for comments
     */
    public static final char COMMENT = '#';
    /**
     * comma for arguments separation
     */
    public static final char COMMA = ',';

    private Punctuation() {
        assert false : "utility constructor";
    }

    /**
     * Get the punctuation
     *
     * @return array of punctuation in definition order
     */
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
