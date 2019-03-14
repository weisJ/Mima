package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ProcessorException extends ParserException {

    /**
     * ParsingExpression
     *
     * @param message exception message
     */
    public ProcessorException(String message) {
        super(message, 0, 0, 0);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
