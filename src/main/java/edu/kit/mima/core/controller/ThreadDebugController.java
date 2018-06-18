package edu.kit.mima.core.controller;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ThreadDebugController implements DebugController {

    private static final int SLEEP_TIME = 200;
    private Thread workingThread;
    private boolean isWorking;

    public ThreadDebugController(Thread workingThread) {
        this.workingThread = workingThread;
        isWorking = false;
    }

    public ThreadDebugController() {
        isWorking = false;
    }


    public void setWorkingThread(Thread workingThread) {
        this.workingThread = workingThread;
        isWorking = false;
    }

    public boolean isWorking() {
        return isWorking;
    }

    @Override
    public void pause() {
        isWorking = false;
        boolean interrupted = false;
        while (!interrupted) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                interrupted = true;
                isWorking = true;
            }
        }
    }

    @Override
    public void resume() {
        if (workingThread == null) {
            return;
        }
        workingThread.interrupt();
    }

    @Override
    public void start() {
        if (workingThread == null) {
            return;
        }
        workingThread.start();
    }

    @Override
    public void stop() {
        if (workingThread == null) {
            return;
        }
        isWorking = false;
        try {
            workingThread.interrupt();
            workingThread.join();
        } catch (InterruptedException ignored) { /*doesn't matter thread should die*/}
    }
}
