package edu.kit.mima.core;

import edu.kit.mima.api.logging.DefaultLogger;
import edu.kit.mima.api.logging.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Defaults for the Mima Core.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaCoreDefaults {

    private static Logger logger = new DefaultLogger();

    /**
     * Get the logger.
     *
     * @return logger.
     */
    @Contract(pure = true)
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Set the logger.
     *
     * @param logger logger to use.
     */
    public static void setLogger(@Nullable final Logger logger) {
        MimaCoreDefaults.logger = Objects.requireNonNullElseGet(logger, DefaultLogger::new);
    }
}
