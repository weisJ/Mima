package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.List;

/**
 * MimaController uses {@link Mima} and {@link Interpreter} to
 * execute custom mima programs
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaController {

    private static final int WORD_LENGTH = 24;
    private static final int CONST_WORD_LENGTH = 20;

    private final Interpreter interpreter;
    private final Mima mima;
    private ProgramToken programToken;

    /**
     * Create new MimaController instance
     */
    public MimaController() {
        interpreter = new Interpreter(WORD_LENGTH);
        mima = new Mima(WORD_LENGTH, WORD_LENGTH);
    }

    /**
     * Parse the program given program. Can then be run using the
     * run() method
     *
     * @param program program to parse
     */
    public void parse(String program) {
        programToken = new Parser(program).parse();
    }

    /**
     * Run the program
     */
    public void run() {
        interpreter.evaluateTopLevel(programToken, setupGlobalEnvironment(programToken));
    }

    /*
     * Define the global instructions
     */
    private Environment setupGlobalEnvironment(ProgramToken programToken) {
        Environment globalEnv = new Environment(null, programToken);
        MimaInstruction.setMima(mima);
        MimaXInstruction.setMima(mima);
        //Default mima instructions
        setupDefaultInstructions(globalEnv);
        for (MimaInstruction instruction : MimaInstruction.values()) {
            globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
        //MimaX instructions
        setupExtendedInstructionSet(globalEnv);
        return globalEnv;
    }

    private void setupDefaultInstructions(Environment environment) {
        //Halt Instruction
        environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "HALT"), (args, env) -> {
            checkArgNumber(args, 0);
            interpreter.setRunning(false);
            return null;
        });
        //Jump Instruction
        environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMP"), (args, env) -> {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            if (argument.getType() != ValueType.JUMP_REFERENCE) {
                throw new IllegalArgumentException("must pass jump reference");
            }
            interpreter.performJump(argument.getValue().intValue());
            return null;
        });
        //Jump if negative Instruction
        environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "JMN"), (args, env) -> {
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

    private void setupExtendedInstructionSet(Environment environment) {
        //CALL subroutine
        environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "CALL"), (args, env) -> {
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
        environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, "RET"), (args, env) -> {
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
            environment.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
    }

    private void checkArgNumber(List<Value<MachineWord>> args, int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            throw new IllegalArgumentException("invalid number of arguments");
        }
    }
}
