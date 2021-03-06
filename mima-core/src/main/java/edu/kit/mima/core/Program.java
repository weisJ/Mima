package edu.kit.mima.core;

import edu.kit.mima.api.util.ImmutableTuple;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.token.ProgramToken;

/**
 * Program class that contains the parsed {@link ProgramToken} and its corresponding {@link
 * InstructionSet}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Program implements ImmutableTuple<ProgramToken, InstructionSet> {

    private final ProgramToken programToken;
    private final InstructionSet instructionSet;

    /**
     * Create new program. It consists of a programToken and an instructionSet
     *
     * @param programToken   programToken
     * @param instructionSet instructionSet
     */
    public Program(final ProgramToken programToken, final InstructionSet instructionSet) {
        this.instructionSet = instructionSet;
        this.programToken = programToken;
    }

    /**
     * Get program Token.
     *
     * @return programToken
     */
    public ProgramToken getProgramToken() {
        return programToken;
    }

    /**
     * Get InstructionSet.
     *
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
