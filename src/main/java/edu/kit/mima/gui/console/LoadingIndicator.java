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
    private Timer timer;
    private boolean running = false;

    public void start(String startMessage, int dotsNumber) {
        if (running) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new LoadingTask(startMessage, dotsNumber),0, PERIOD);
        running = true;
    }

    public void stop(String lastMessage) {
        if (!running) {
            return;
        }
        timer.cancel();
        timer.purge();
        Logger.log(lastMessage, true);
        running = false;
    }

    public void error(String message) {
        timer.cancel();
        timer.purge();
        Logger.error(message, true);
    }

    private class LoadingTask extends TimerTask {
        int counter = 0;
        private boolean first = true;
        private final StringBuilder message;
        private final int dotsPeriod;

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
            Logger.log(message.toString() + dots.toString(), !first);
            if (first) first = false;
            counter++;
        }
    }
}
