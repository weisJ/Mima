package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class InterpretationException extends IllegalArgumentException {

    private final String message;
    private final String line;
    private final int lineNumber;
    private final boolean number;

    public InterpretationException(final String message, final String line, final int lineNumber) {
        super();
        this.message = message;
        this.line = line;
        this.lineNumber = lineNumber;
        this.number = true;
    }

    public InterpretationException(final String message, final int lineNumber) {
        super();
        this.message = message;
        this.line = null;
        this.lineNumber = lineNumber;
        this.number = true;
    }

    public InterpretationException(final String message, final String line) {
        super();
        this.message = message;
        this.line = line;
        this.lineNumber = 0;
        this.number = false;
    }

    public InterpretationException(final String message) {
        super();
        this.message = message;
        this.line = null;
        this.lineNumber = 0;
        this.number = false;
    }

    @Override
    public String getMessage() {
        String cause = message;
        cause = number ? (cause + " | line: " + lineNumber) : cause;
        cause = (line == null) ? cause : (cause + " <" + line + ">");
        return cause;
    }
}
