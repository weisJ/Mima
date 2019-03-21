package edu.kit.mima.core.interpretation.stack;

/**
 * A Continuation should be thrown if the Execution stack has reached its limit. It holds the
 * executable Continuation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Continuation extends RuntimeException {

    private final Runnable continuation;

    /**
     * Create new Continuation.
     *
     * @param continuation executable continuation function.
     */
    public Continuation(final Runnable continuation) {
        this.continuation = continuation;
    }

    /**
     * Get the executable continuation.
     *
     * @return the continuation.
     */
    public Runnable getContinuation() {
        return continuation;
    }
}
