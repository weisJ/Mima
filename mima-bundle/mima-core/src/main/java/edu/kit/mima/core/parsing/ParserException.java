package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.inputstream.TokenStream;

/**
 * Gets thrown by {@link CharInputStream}, {@link TokenStream} or {@link Parser}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ParserException extends RuntimeException {

    protected final String message;
    private final int line;
    private final int column;
    private final int position;

    /**
     * Create new ParsingExpression.
     *
     * @param message  exception message
     * @param line     line in which the exception happened
     * @param column   column in which the exception happened
     * @param position absolute position in file
     */
    public ParserException(
            final String message, final int line, final int column, final int position) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.position = position;
    }

    @Override
    public String getMessage() {
        return message + "(line: " + line + "|column: " + column + ')';
    }

    public int getPosition() {
        return position;
    }
}
