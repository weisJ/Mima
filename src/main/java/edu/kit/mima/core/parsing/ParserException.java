package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

/**
 * Gets thrown by {@link CharInputStream}, {@link TokenStream} or {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ParserException extends IllegalArgumentException {

    private final int line;
    private final int column;
    private final String message;

    /**
     * ParsingExpression
     *
     * @param message exception message
     * @param line    line in which the exception happened
     * @param column  column in which the exception happened
     */
    public ParserException(String message, int line, int column) {
        this.line = line;
        this.column = column;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message + "(line: " + line + "|column: " + column + ')';
    }
}
