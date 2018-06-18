package edu.kit.mima.core.interpretation;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.List;

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
     * @param mima the {@link Mima} for this global environment
     * @param interpreter the {@link Interpreter} for this global environment
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
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "HALT"), (args, env) -> {
            checkArgNumber(args, 0);
            interpreter.setRunning(false);
            return null;
        });
        //Jump Instruction
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMP"), (args, env) -> {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            if (argument.getType() != ValueType.JUMP_REFERENCE) {
                throw new IllegalArgumentException("must pass jump reference");
            }
            interpreter.performJump(argument.getValue().intValue());
            return null;
        });
        //Jump if negative Instruction
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMN"), (args, env) -> {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            if (argument.getType() != ValueType.JUMP_REFERENCE) {
                throw new IllegalArgumentException("must pass jump reference");
            }
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
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "CALL"), (args, env) -> {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            if (argument.getType() != ValueType.JUMP_REFERENCE) {
                throw new IllegalArgumentException("must pass jump reference");
            }
            mima.pushRoutine(env.getExpressionIndex() + 1, env);
            interpreter.performJump(argument.getValue().intValue());
            return null;
        });
        //Return from subroutine
        defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "RET"), (args, env) -> {
            checkArgNumber(args, 0);
            if (mima.hasEmptyReturnStack()) {
                throw new IllegalArgumentException("nowhere to return to");
            }
            var pair = mima.returnRoutine();
            interpreter.prepareJump(pair.getValue());
            interpreter.performJump(pair.getKey());
            return null;
        });
        for (MimaXInstruction instruction : MimaXInstruction.values()) {
            defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
    }

    /**
     * Check the argument for given number of arguments
     *
     * @param args                   arguments list
     * @param expectedArgumentNumber expected number of arguments
     */
    private static void checkArgNumber(List<Value<MachineWord>> args, int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            throw new IllegalArgumentException("invalid number of arguments");
        }
    }
}
