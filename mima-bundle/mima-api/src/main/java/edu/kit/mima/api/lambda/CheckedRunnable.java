package edu.kit.mima.api.lambda;

/**
 * Runable that can throw n exception.
 *
 * @author Jannis Weis
 * @since 2019
 */
@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

    void run() throws E;
}
