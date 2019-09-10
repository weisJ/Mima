package edu.kit.mima.core.parsing;

/**
 * Exception that gets thrown during processing by {@link Processor}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProcessorException extends ParserException {

    /**
     * ParsingExpression.
     *
     * @param message exception message
     */
    public ProcessorException(final String message) {
        super(message, 0, 0, 0);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
