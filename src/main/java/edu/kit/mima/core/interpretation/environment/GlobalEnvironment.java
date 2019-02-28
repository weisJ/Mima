package edu.kit.mima.core.interpretation.environment;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.instruction.InstructionTools;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.parsing.token.ProgramToken;

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
            defineFunction(instruction.toString(), instruction);
        }
    }

    /**
     * Setup the default instruction set from {@link MimaInstruction} for the given environment
     */
    private void setupDefaultInstructions() {
        //Halt Instruction
        defineNewFunction("HALT", 0, (args, env, callback) -> interpreter.setRunning(false));
        //Jump Instruction
        defineNewFunction("JMP", 1, (args, env, callback) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            Environment jumpEnv = env.lookupJump(argument.getValue().toString());
            int jumpIndex = env.getJump(argument.getValue().toString());
            interpreter.jump(jumpEnv, jumpIndex, callback);
        });
        //Jump if negative Instruction
        defineNewFunction("JMN", 1, (args, env, callback) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            Environment jumpEnv = env.lookupJump(argument.getValue().toString());
            int jumpIndex = env.getJump(argument.getValue().toString());
            if (mima.getAccumulator().msb() == 1) {
                interpreter.jump(jumpEnv, jumpIndex, callback);
            } else {
                callback.accept(new Value<>(ValueType.NUMBER, 0));
            }
        });
    }

    /**
     * Setup the default extended instruction set from {@link MimaXInstruction} for the given environment
     */
    public void setupExtendedInstructionSet() {
        //CALL subroutine
        defineNewFunction("CALL", 1, (args, env, callback) -> {
            var argument = InstructionTools.getJumpReference(args, 0);
            mima.pushRoutine(env.getExpressionIndex() + 1, env);
            Environment jumpEnv = env.lookupJump(argument.getValue().toString());
            int jumpIndex = env.getJump(argument.getValue().toString());
            interpreter.jump(jumpEnv, jumpIndex, callback);
        });
        //Return from subroutine
        defineNewFunction("RET", 0, (args, env, callback) -> {
            if (mima.hasEmptyReturnStack()) {
                throw new IllegalArgumentException("nowhere to return to");
            }
            var pair = mima.returnRoutine();
            interpreter.jump(pair.getValue(), pair.getKey(), callback);
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
        defineFunction(name, (args, env, callback) -> {
            InstructionTools.checkArgNumber(args, argNum);
            instruction.apply(args, env, callback);
        });
    }
}
