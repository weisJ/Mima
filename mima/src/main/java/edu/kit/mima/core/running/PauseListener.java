package edu.kit.mima.core.running;

import edu.kit.mima.core.parsing.token.Token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface PauseListener {

    void paused(Token currentInstruction);
}
