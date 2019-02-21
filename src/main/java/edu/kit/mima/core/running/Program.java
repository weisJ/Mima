package edu.kit.mima.core.running;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.token.ImmutableTuple;
import edu.kit.mima.core.parsing.token.ProgramToken;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Program implements ImmutableTuple<ProgramToken, InstructionSet> {

    private final ProgramToken programToken;
    private final InstructionSet instructionSet;

    /**
     * Create new program. It consists of a programToken and an instructionSet
     * @param programToken programToken
     * @param instructionSet instructionSet
     */
    public Program(ProgramToken programToken, InstructionSet instructionSet) {
        this.instructionSet = instructionSet;
        this.programToken = programToken;
    }

    /**
     * Get program Token.
     * @return programToken
     */
    public ProgramToken getProgramToken() {
        return programToken;
    }

    /**
     * Get InstructionSet
     * @return instructionSet
     */
    public InstructionSet getInstructionSet() {
        return instructionSet;
    }

    @Override
    public ProgramToken getFirst() {
        return getProgramToken();
    }

    @Override
    public InstructionSet getSecond() {
        return getInstructionSet();
    }
}
