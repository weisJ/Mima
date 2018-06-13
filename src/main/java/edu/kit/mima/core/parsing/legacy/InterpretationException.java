package edu.kit.mima.core.parsing.legacy;

import org.jetbrains.annotations.Nullable;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class InterpretationException extends IllegalArgumentException {

    private final String message;
    @Nullable
    private final String line;
    private final int lineNumber;
    private final boolean number;

    public InterpretationException(final String message, @Nullable final String line, final int lineNumber) {
        super();
        this.message = message;
        this.line = line;
        this.lineNumber = lineNumber;
        number = true;
    }

    public InterpretationException(final String message, final int lineNumber) {
        super();
        this.message = message;
        line = null;
        this.lineNumber = lineNumber;
        number = true;
    }

    public InterpretationException(final String message, @Nullable final String line) {
        super();
        this.message = message;
        this.line = line;
        lineNumber = 0;
        number = false;
    }

    public InterpretationException(final String message) {
        super();
        this.message = message;
        line = null;
        lineNumber = 0;
        number = false;
    }

    @Override
    public String getMessage() {
        String cause = message;
        cause = number ? (cause + " | line: " + lineNumber) : cause;
        cause = (line == null) ? cause : (cause + " <" + line + '>');
        return cause;
    }
}
