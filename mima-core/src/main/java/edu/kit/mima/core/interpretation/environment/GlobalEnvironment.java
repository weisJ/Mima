package edu.kit.mima.core.interpretation.environment;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.Mima;
import edu.kit.mima.core.Program;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.Instruction;
import edu.kit.mima.core.instruction.InstructionTools;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Global Environment for execution. Provides higher level instructions such as JMP, HALT, CALL, and
 * RET.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class GlobalEnvironment extends Environment {

    private final Consumer<Value<?>> callback;
    private final Interpreter interpreter;
    @NotNull
    private final Mima mima;

    /**
     * Create global environment.
     *
     * @param program programToken for this environment
     * @param mima         the {@link Mima} for this global environment
     * @param interpreter  the {@link Interpreter} for this global environment
     * @param callback     final callback when halting the program.
     */
    public GlobalEnvironment(final Program program,
                             @NotNull final Mima mima,
                             final Interpreter interpreter,
                             final Consumer<Value<?>> callback) {
        super(null, program.getProgramToken(), program.getInstructionSet(),
              0, program.getProgramToken().getLength());
        this.mima = mima;
        this.interpreter = interpreter;
        this.callback = callback;
        MimaInstruction.setMima(mima);
        MimaXInstruction.setMima(mima);
        setupDefaultInstructions();
    }

    /**
     * Setup globally accessible functions.
     *
     * @param instructions array of instructions to add.
     */
    public void setupGlobalFunctions(@NotNull final Instruction[] instructions) {
        for (final Instruction instruction : instructions) {
            defineFunction(instruction.toString(), instruction);
        }
    }

    /**
     * Setup the default instruction set from {@link MimaInstruction} for the given environment.
     */
    private void setupDefaultInstructions() {
        // Halt Instruction
        defineNewFunction("HALT", 0, (args, env, call) -> {
            interpreter.setRunning(false);
            GlobalEnvironment.this.callback.accept(new Value<>(ValueType.NUMBER, mima.getAccumulator()));
        });
        // Jump Instruction
        defineNewFunction("JMP", 1, (args, env, call) -> {
            final Tuple<Environment, Integer> info = getJumpInformation(args, env);
            interpreter.jump(info.getFirst(), info.getSecond(), call);
        });
        // Jump if negative Instruction
        defineNewFunction("JMN", 1, (args, env, call) -> {
            final Tuple<Environment, Integer> info = getJumpInformation(args, env);
            if (mima.getAccumulator().msb() == 1) {
                interpreter.jump(info.getFirst(), info.getSecond(), call);
            } else {
                call.accept(new Value<>(ValueType.NUMBER, 0));
            }
        });
        // Jump Indirect Instruction
        defineNewFunction("JIND", 1, (args, env, call) -> {
            final var argument = InstructionTools.getMemoryReference(args, 0);
            final MachineWord target = mima.loadValue(((MachineWord) argument.getValue()).intValue());
            final Tuple<Environment, Integer> info = getJumpInformation(
                    List.of(new Value<>(ValueType.NUMBER, target)), env);
            interpreter.jump(info.getFirst(), info.getSecond(), call);
        });
    }

    @NotNull
    private Tuple<Environment, Integer> getJumpInformation(@NotNull final List<Value<?>> args,
                                                           @NotNull final Environment env) {
        final var argument = InstructionTools.getJumpReference(args, 0);
        final Environment jumpEnv;
        final int jumpIndex;
        if (argument.getType() == ValueType.JUMP_REFERENCE) {
            jumpEnv = env.lookupJump(argument.getValue().toString());
            jumpIndex = env.getJump(argument.getValue().toString());
        } else {
            final int tokenIndex = ((MachineWord) argument.getValue()).intValue();
            jumpEnv = env.lookupToken(tokenIndex);
            final var indexList = jumpEnv.getProgramToken().getIndexList();
            int jump = Collections.binarySearch(indexList, tokenIndex);
            int off = 0;
            while (jump + off < indexList.size() && indexList.get(jump + off) == tokenIndex) {
                off++;
            }
            jumpIndex = jump - Math.min(0, off - 1);
        }
        return new ValueTuple<>(jumpEnv, jumpIndex);
    }

    /**
     * Setup the default extended instruction set from {@link MimaXInstruction} for the given
     * environment.
     */
    public void setupExtendedInstructionSet() {
        // CALL subroutine
        defineNewFunction("CALL", 1, (args, env, call) -> {
            final var argument = InstructionTools.getJumpReference(args, 0);
            mima.pushRoutine(env.getExpressionIndex() + 1, env);
            final Environment jumpEnv = env.lookupJump(argument.getValue().toString());
            final int jumpIndex = env.getJump(argument.getValue().toString());
            interpreter.jump(jumpEnv, jumpIndex, call);
        });
        // Return from subroutine
        defineNewFunction("RET", 0, (args, env, call) -> {
            if (mima.hasEmptyReturnStack()) {
                throw new IllegalStateException("nowhere to return to");
            }
            final var pair = mima.returnRoutine();
            interpreter.jump(pair.getSecond(), pair.getFirst(), call);
        });
    }

    /**
     * Create and define new Function.
     *
     * @param name        name of function
     * @param argNum      number of arguments of function
     * @param instruction execution instructions
     */
    private void defineNewFunction(@NotNull final String name, final int argNum,
                                   @NotNull final Instruction instruction) {
        defineFunction(name, (args, env, call) -> {
            InstructionTools.checkArgNumber(args, argNum);
            instruction.apply(args, env, call);
        });
    }
}
