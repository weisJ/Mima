package edu.kit.mima.core;

import edu.kit.mima.core.token.ProgramToken;

/**
 * Event handler for compilation events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface CompilationEventHandler {

    /**
     * Method to hook into an compilation process.
     *
     * @param programToken output programToken.
     */
    void notifyCompilation(ProgramToken programToken);
}
