package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum TokenType {

    /*Higher level types*/
    /**
     * Program type
     */
    PROGRAM,
    /**
     * Function type
     */
    CALL,
    /**
     * Jump point type
     */
    JUMP_POINT,
    /**
     * Resolved JumpPoint type
     */
    RESOLVED_JUMP_POINT,

    /*Lower level types*/
    /**
     * Keyword type
     */
    KEYWORD,
    /**
     * Punctuation type
     */
    PUNCTUATION,
    /**
     * Number type
     */
    NUMBER,
    /**
     * Binary number type
     */
    BINARY,

    /**
     * Identification type
     */
    IDENTIFICATION,
    /**
     * Definition type
     */
    DEFINITION,
    /**
     * Constant type
     */
    CONSTANT,

    /*Utility types*/
    /**
     * Array type
     */
    ARRAY,
    /**
     * Empty type
     */
    EMPTY,
}
