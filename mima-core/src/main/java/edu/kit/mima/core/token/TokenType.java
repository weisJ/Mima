package edu.kit.mima.core.token;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import org.jetbrains.annotations.Contract;

/**
 * Possible types of {@link Token}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum TokenType {

    /*Higher level types*/
    /**
     * Program type.
     */
    PROGRAM,
    /**
     * Function type.
     */
    CALL,
    /**
     * Jump point type.
     */
    JUMP_POINT,

    /*Lower level types*/
    /**
     * Keyword type.
     */
    KEYWORD,
    /**
     * Punctuation type.
     */
    PUNCTUATION,
    /**
     * Number type.
     */
    NUMBER,
    /**
     * Binary number type.
     */
    BINARY(String.valueOf(Punctuation.BINARY_PREFIX)),
    /**
     * String type.
     */
    STRING,
    /**
     * Comment type.
     */
    COMMENT(String.valueOf(Punctuation.COMMENT)),
    /**
     * new line type.
     */
    NEW_LINE,

    /**
     * Identification type.
     */
    IDENTIFICATION,
    /**
     * Definition type.
     */
    DEFINITION(Keyword.DEFINITION + " "),
    /**
     * Constant type.
     */
    CONSTANT(Keyword.CONSTANT + " "),
    /**
     * Reference type.
     */
    REFERENCE("var "),

    /*Utility types*/
    /**
     * Array type.
     */
    ARRAY,
    /**
     * Empty type.
     */
    EMPTY,
    /**
     * Error type. Used internally for signalling the parsing failed.
     */
    ERROR,
    /**
     * End of Scope.
     */
    SCOPE_END;

    private final String prefix;

    @Contract(pure = true)
    TokenType(final String prefix) {
        this.prefix = prefix;
    }

    @Contract(pure = true)
    TokenType() {
        this("");
    }

    @Contract(pure = true)
    public String getPrefix() {
        return prefix;
    }
}
