package edu.kit.mima.core.token;

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
    BINARY,
    /**
     * String type.
     */
    STRING,
    /**
     * Comment type.
     */
    COMMENT,
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
    DEFINITION,
    /**
     * Constant type.
     */
    CONSTANT,

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
    SCOPE_END
}
