package edu.kit.mima.gui.console;

import edu.kit.mima.gui.logging.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LoadingIndicator {

    private static final int PERIOD = 500;
    private static Timer timer;
    private static boolean running = false;

    private LoadingIndicator() {
        assert false : "utility class constructor";
    }

    public static void start(String startMessage, int dotsNumber) {
        if (running) {
            return;
        }
        timer = new Timer();
        Logger.log(startMessage);
        timer.scheduleAtFixedRate(new LoadingTask(startMessage, dotsNumber), 0, PERIOD);
        running = true;
    }

    public static void stop(String lastMessage) {
        if (!running) {
            return;
        }
        timer.cancel();
        timer.purge();
        Logger.log(lastMessage, true);
        running = false;
    }

    public static void error(String message) {
        timer.cancel();
        timer.purge();
        Logger.error(message, true);
        running = false;
    }

    private static class LoadingTask extends TimerTask {
        private final StringBuilder message;
        private final int dotsPeriod;
        int counter = 0;

        private LoadingTask(String message, int dotsNumber) {
            this.message = new StringBuilder(message);
            this.dotsPeriod = dotsNumber + 1;
        }

        @Override
        public void run() {
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < counter % dotsPeriod; i++) {
                dots.append('.');
            }
            Logger.log(message.toString() + dots.toString(), true);
            counter++;
        }
    }
}
