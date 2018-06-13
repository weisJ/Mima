package edu.kit.mima.core.parsing.lang;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum Punctuation {

    DEFINITION_BEGIN('§'),
    DEFINITION_DELIMITER(':'),
    OPEN_BRACKET('('),
    CLOSED_BRACKET(')'),
    INSTRUCTION_END(';'),
    BINARY_PREFIX('~'),
    COMMENT('#');

    private final char punctuation;

    Punctuation(char punctuation) {
        this.punctuation = punctuation;
    }

    public char getPunctuation() {
        return punctuation;
    }
}
