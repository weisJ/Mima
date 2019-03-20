package edu.kit.mima.logging;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of an Loading Indicator that logs messages using the {@link Logger} functions.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class LoadingIndicator {

    /**
     * Time period in which a new messageBuilder get logged.
     */
    private static final int PERIOD = 500;
    /**
     * Timer for handling the logging.
     */
    private static Timer timer;
    /**
     * Indicator if Indicator is running.
     */
    private static boolean running = false;

    /**
     * {@link LoadingIndicator} should not be instantiated.
     */
    @Contract(" -> fail")
    private LoadingIndicator() {
        assert false : "utility class constructor";
    }

    /**
     * Start the indicator.
     *
     * @param startMessage initial messageBuilder
     * @param dotsNumber   number of loading dots
     */
    public static void start(@NotNull final String startMessage, final int dotsNumber) {
        if (running) {
            return;
        }
        timer = new Timer();
        Logger.log(startMessage);
        Logger.setLock(true);
        timer.scheduleAtFixedRate(
                new LoadingTask(startMessage, dotsNumber), 0, PERIOD);
        running = true;
    }

    /**
     * Stop the indicator.
     *
     * @param lastMessage last messageBuilder to log.
     */
    public static void stop(final String lastMessage) {
        if (!running) {
            return;
        }
        timer.cancel();
        timer.purge();
        Logger.log(lastMessage, true);
        Logger.setLock(false);
        running = false;
    }

    /**
     * Stop Indicator with error messageBuilder.
     *
     * @param message error messageBuilder
     */
    public static void error(final String message) {
        timer.cancel();
        timer.purge();
        Logger.error(message, true);
        Logger.setLock(false);
        running = false;
    }

    private static final class LoadingTask extends TimerTask {
        /**
         * {@link StringBuilder} to create messages.
         */
        @NotNull
        private final StringBuilder messageBuilder;
        /**
         * Period after which number of dots should jump back to 0.
         */
        private final int dotsPeriod;
        /**
         * Counter keeping track of current number of dots.
         */
        private int counter = 0;

        /**
         * Task to create the messages.
         *
         * @param message    initial messageBuilder
         * @param dotsNumber number of dots to print
         */
        private LoadingTask(@NotNull final String message, final int dotsNumber) {
            this.messageBuilder = new StringBuilder(message);
            this.dotsPeriod = dotsNumber + 1;
        }

        @Override
        public void run() {
            Logger.log(messageBuilder.toString() + ".".repeat(counter % dotsPeriod), true);
            counter++;
        }
    }
}
