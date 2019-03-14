package edu.kit.mima.core.interpretation.stack;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Continuation extends RuntimeException {

    private final Runnable continuation;

    public Continuation(Runnable continuation) {
        this.continuation = continuation;
    }

    public Runnable getContinuation() {
        return continuation;
    }
}
