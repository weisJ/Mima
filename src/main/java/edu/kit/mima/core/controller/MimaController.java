package edu.kit.mima.core.controller;

import edu.kit.mima.core.Mima;
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
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MimaController uses {@link Mima} and {@link Interpreter} to
 * execute custom mima programs
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaController {

    private Interpreter interpreter;
    private Mima mima;
    private ProgramToken programToken;
    private Environment globalEnvironment;

    private InstructionSet currentInstructionSet;

    /**
     * Create new MimaController instance
     */
    public MimaController() {
        interpreter = new Interpreter(InstructionSet.MIMA.getConstCordLength());
        mima = new Mima(InstructionSet.MIMA.getWordLength(), InstructionSet.MIMA.getConstCordLength());
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

    /**
     * Parse the program given program. Can then be run using the
     * run() method
     *
     * @param program        program to parse
     * @param instructionSet instructionSet to use
     */
    public void parse(String program, InstructionSet instructionSet) {
        currentInstructionSet = instructionSet;
        interpreter = new Interpreter(instructionSet.getConstCordLength());
        mima = new Mima(instructionSet.getWordLength(), instructionSet.getConstCordLength());
        programToken = new Parser(program).parse();
        Token lastToken = programToken.getValue()[programToken.getValue().length - 1];
        if (lastToken.getType() == TokenType.ERROR) {
            throw new IllegalArgumentException(lastToken.getValue().toString());
        }
        globalEnvironment = createGlobalEnvironment(programToken);
        if (instructionSet == InstructionSet.MIMA_X) {
            setupExtendedInstructionSet(globalEnvironment);
        }
    }

    /**
     * Returns whether the underlying interpreter is running
     *
     * @return true if running
     */
    public boolean isRunning() {
        return interpreter.isRunning();
    }

    /**
     * Run the program
     */
    public void run() {
        if (!interpreter.isRunning()) {
            start(false);
        }
        while (interpreter.isRunning()) {
            Thread.onSpinWait();
        }
    }

    /**
     * Perform one step of the program
     */
    public void step() {
        if (!interpreter.isRunning()) {
            start(true);
        }
        interpreter.resume();
        while (interpreter.isWorking()) {
            Thread.onSpinWait();
        }
    }

    /**
     * Start the interpreter
     *
     * @param debug whether the interpreter should pause after each statement
     */
    private void start(boolean debug) {
        if (programToken == null || globalEnvironment == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        interpreter.evaluateTopLevel(programToken, globalEnvironment, debug);
    }

    /**
     * Stop the interpreter
     */
    public void stop() {
        interpreter.setRunning(false);
        interpreter.resume();
    }

    /**
     * Reset the program to the beginning
     */
    public void reset() {
        stop();
        mima.reset();
    }

    /**
     * Create the global environment
     *
     * @param programToken programToken for the environment
     * @return created global environment
     */
    private Environment createGlobalEnvironment(ProgramToken programToken) {
        Environment globalEnv = new Environment(null, programToken);
        MimaInstruction.setMima(mima);
        MimaXInstruction.setMima(mima);
        //Default mima instructions
        setupDefaultInstructions(globalEnv);
        for (MimaInstruction instruction : MimaInstruction.values()) {
            globalEnv.defineFunction(new AtomToken<>(TokenType.IDENTIFICATION, instruction.toString()), instruction);
        }
        return globalEnv;
    }

    /**
     * Setup the default instruction set from {@link MimaInstruction} for the given environment
     *
     * @param environment environment to setup
     */
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

    /**
     * Setup the extended instruction set from {@link MimaXInstruction} for the given environment
     *
     * @param environment environment to setup
     */
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

    /**
     * Get the current instruction token
     *
     * @return current token
     */
    public Token getCurrent() {
        return interpreter.getCurrentToken();
    }

    /**
     * Get the currently used instruction set
     *
     * @return instruction set
     */
    public InstructionSet getInstructionSet() {
        return currentInstructionSet;
    }

    /**
     * Get the current mima memory table
     *
     * @return memory table
     */
    public Object[][] getMemoryTable() {
        Environment scope = interpreter.getCurrentScope();
        scope = scope == null || !interpreter.isRunning() ? globalEnvironment : scope;
        Map<String, Integer> map = scope.getVariables().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getValue().toString(), e -> e.getValue().intValue()));
        return mima.memoryTable(map);
    }

    /**
     * Search the parsed program for references and return as List of Sets
     * as follows {{constant references}, {jump references}, {memory references}}
     *
     * @return list with references sets
     */
    public List<Set<String>> getReferences() {
        Set<String> memory = new HashSet<>();
        Set<String> constants = new HashSet<>();
        Set<String> jumps = new HashSet<>();
        Token[] tokens = programToken.getValue();
        for (Token token : tokens) {
            searchReferences(token, memory, constants, jumps);
        }
        return List.of(constants, jumps, memory);
    }

    private void searchReferences(Token token, Set<String> memory, Set<String> constants, Set<String> jump) {
        TokenType tokenType = token.getType();
        switch (tokenType) {
            case PROGRAM:
                Token[] tokens = programToken.getValue();
                for (Token t : tokens) {
                    searchReferences(t, memory, constants, jump);
                }
                break;
            case JUMP_POINT:
                jump.add(((Token) token.getValue()).getValue().toString());
                break;
            case DEFINITION:
                memory.add(((Token) token.getValue()).getValue().toString());
                break;
            case CONSTANT:
                constants.add(((Token) token.getValue()).getValue().toString());
                break;
            default:
                break;
        }
    }
}
