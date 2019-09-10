package edu.kit.mima.core.interpretation.stack;

/**
 * The StackGuard is responsible for preventing the call stack from overflowing. If the stack has
 * reached a certain limit a {@link Continuation} gets thrown that must be handled.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class StackGuard {

    private static final int INITIAL_LENGTH = 200;
    private int stackLength;

    /**
     * Create new StackGuard. StackGuard can guard function to prevent the call stack from
     * overflowing.
     */
    public StackGuard() {
        stackLength = INITIAL_LENGTH;
    }

    /**
     * Reset the stackLength.
     */
    public void reset() {
        stackLength = INITIAL_LENGTH;
    }

    /**
     * Guard a function call.
     *
     * @param callback callback to invoke when continuation is thrown
     * @throws Continuation Continuation with function to invoke.
     */
    public void guard(final Runnable callback) throws Continuation {
        stackLength--;
        if (stackLength < 0) {
            throw new Continuation(callback);
        }
    }
}
