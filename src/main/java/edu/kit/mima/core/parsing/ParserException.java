package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ParserException extends IllegalArgumentException {

    private final int line;
    private final int column;
    private final String message;

    public ParserException(String message, int line, int column) {
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public String getMessage() {
        return message + "(" + line + ":" + column + ")";
    }
}
