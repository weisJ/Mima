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
     * Native instruction call type
     */
    NATIVE_CALL,
    /**
     * Jump point type
     */
    JUMP_POINT,

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
     * Function type
     */
    FUNCTION,
    /**
     * Number type
     */
    NUMBER,
    /**
     * Binary number type
     */
    BINARY,

    /**
     * Instruction type
     */
    INSTRUCTION,
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
