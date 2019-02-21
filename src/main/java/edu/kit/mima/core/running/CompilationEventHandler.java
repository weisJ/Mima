package edu.kit.mima.core.running;

import edu.kit.mima.core.parsing.token.ProgramToken;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface CompilationEventHandler {

    /**
     * Method to hook into an compilation process.
     * @param programToken output programToken.
     */
    void notifyCompilation(ProgramToken programToken);
}
