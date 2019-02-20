package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.instruction.InstructionTools;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.TokenType;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class GlobalEnvironment extends Environment {

    private final Interpreter interpreter;
    private final Mima mima;

    /**
     * Create global environment
     *
     * @param programToken programToken for this environment
     * @param mima         the {@link Mima} for this global environment
     * @param interpreter  the {@link Interpreter} for this global environment
     */
    public GlobalEnvironment(ProgramToken programToken, Mima mima, Interpreter interpreter) {
        super(null, programToken);
        this.mima = mima;
        this.interpreter = interpreter;
        MimaInstruction.setMima(mima);
        MimaXInstruction.setMima(mima);
        setupDefaultInstructions();
    }

    public void setupGlobalFunctions(Instruction[] instructions) {
        for (Instruction instruction : instructions) {
            defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
    }

    /**
     * Setup the default instruction set from {@link MimaInstruction} for the given environment
     */
    private void setupDefaultInstructions() {
        //Halt Instruction
        defineNewFunction("HALT", 0, (args, env) -> {
            interpreter.setRunning(false);
            return null;
        });
        //Jump Instruction
        defineNewFunction("JMP", 1, (args, env) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            interpreter.performJump(argument.getValue().intValue());
            return null;
        });
        //Jump if negative Instruction
        defineNewFunction("JMN", 1, (args, env) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            if (mima.getAccumulator().msb() == 1) {
                interpreter.performJump(argument.getValue().intValue());
            }
            return null;
        });
    }

    /**
     * Setup the default extended instruction set from {@link MimaXInstruction} for the given environment
     */
    public void setupExtendedInstructionSet() {
        //CALL subroutine
        defineNewFunction("CALL", 1, (args, env) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            mima.pushRoutine(env.getExpressionIndex() + 1, env);
            interpreter.performJump(argument.getValue().intValue());
            return null;
        });
        //Return from subroutine
        defineNewFunction("RET", 0, (args, env) -> {
            if (mima.hasEmptyReturnStack()) {
                throw new IllegalArgumentException("nowhere to return to");
            }
            var pair = mima.returnRoutine();
            interpreter.prepareJump(pair.getValue());
            interpreter.performJump(pair.getKey());
            return null;
        });
    }

    /**
     * Create and define new Function
     *
     * @param name        name of function
     * @param argNum      number of arguments of function
     * @param instruction execution instructions
     */
    private void defineNewFunction(String name, int argNum, Instruction instruction) {
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, name), (args, env) -> {
            InstructionTools.checkArgNumber(args, argNum);
            return instruction.apply(args, env);
        });
    }
}
