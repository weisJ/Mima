package edu.kit.mima.gui.persist;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class PersistenceNotEnabledException extends RuntimeException {

    public PersistenceNotEnabledException(final String message) {
        super(message);
    }
}
