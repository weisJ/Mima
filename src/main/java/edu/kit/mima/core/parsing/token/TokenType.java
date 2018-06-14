package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum TokenType {
    PROGRAM,
    INSTRUCTION,
    CALL,
    JUMP_POINT,

    KEYWORD,
    PUNCTUATION,
    FUNCTION,

    NUMBER,
    BINARY,


    IDENTIFICATION,
    DEFINITION,
    CONSTANT,

    ARRAY,
    EMPTY,
}
