package edu.kit.mima.core.instruction;

import edu.kit.mima.core.parsing.legacy.CompiledInstruction;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Instruction {

    /**
     * Check if an given input matches the instruction
     *
     * @param instruction instruction to match against
     * @return true if matches
     */
    boolean matches(String instruction);

    /**
     * Execute the instruction
     *
     * @param instruction passed value. May be null
     */
    void run(CompiledInstruction instruction);
}
