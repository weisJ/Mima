package edu.kit.mima.api.logging;

/**
 * Default Logger implementation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DefaultLogger implements Logger {
    private LogLevel level = LogLevel.INFO;

    @Override
    public void setLevel(final LogLevel level) {
        this.level = level;
    }

    @Override
    public void log(final String message) {
        if (level == LogLevel.INFO) {
            System.out.println("[INFO]" + message);
        }
    }

    @Override
    public void warning(final String message) {
        if (level != LogLevel.ERROR) {
            System.out.println("[WARNING]" + message);
        }
    }

    @Override
    public void error(final String message) {
        System.err.println("[ERROR]" + message);
    }

    @Override
    public void clear() {
    }
}
