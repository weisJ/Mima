package edu.kit.mima.core.parsing.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Punctuation used in {@link Parser} and {@link TokenStream}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Punctuation {

    /**
     * begin of preprocessor statement.
     */
    char PRE_PROC = '!';
    /**
     * Begin of definition statement.
     */
    char DEFINITION_BEGIN = '§';
    /**
     * definition separator/delimiter.
     */
    char DEFINITION_DELIMITER = '=';
    /**
     * Jump definition delimiter.
     */
    char JUMP_DELIMITER = ':';
    /**
     * Open bracket.
     */
    char OPEN_BRACKET = '(';
    /**
     * Closed bracket.
     */
    char CLOSED_BRACKET = ')';
    /**
     * End of line/instruction.
     */
    char INSTRUCTION_END = ';';
    /**
     * Prefix for binary numbers.
     */
    char BINARY_PREFIX = '~';
    /**
     * prefix for comments.
     */
    char COMMENT = '#';
    /**
     * Comment block modifier.
     */
    char COMMENT_BLOCK_MOD = '*';
    /**
     * comma for arguments separation.
     */
    char COMMA = ',';
    /**
     * open scope bracket.
     */
    char SCOPE_OPEN = '{';
    /**
     * open scope bracket.
     */
    char SCOPE_CLOSED = '}';
    /**
     * String begin and end.
     */
    char STRING = '\'';
    /**
     * Internal jump delimiter.
     */
    char INTERNAL_JUMP = '_';

    /**
     * Get the punctuation.
     *
     * @return array of punctuation in definition order
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    static char[] getPunctuation() {
        return new char[]{
                PRE_PROC,
                DEFINITION_BEGIN,
                DEFINITION_DELIMITER,
                JUMP_DELIMITER,
                OPEN_BRACKET,
                CLOSED_BRACKET,
                INSTRUCTION_END,
                BINARY_PREFIX,
                COMMENT,
                COMMA,
                SCOPE_OPEN,
                SCOPE_CLOSED,
                STRING
        };
    }
}
