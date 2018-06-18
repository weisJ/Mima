package edu.kit.mima.core.controller;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.ExceptionListener;
import edu.kit.mima.core.interpretation.GlobalEnvironment;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * MimaController uses {@link Mima} and {@link Interpreter} to
 * execute custom mima programs
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaController implements ExceptionListener {

    private final AtomicReference<Exception> sharedException;
    private final ThreadDebugController threadDebugController;

    private Interpreter interpreter;
    private Mima mima;
    private ProgramToken programToken;
    private GlobalEnvironment globalEnvironment;

    private InstructionSet currentInstructionSet;

    /**
     * Create new MimaController instance
     */
    public MimaController() {
        interpreter = new Interpreter(0, null, null);
        sharedException = new AtomicReference<>();
        threadDebugController = new ThreadDebugController();
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
        mima = new Mima(instructionSet.getWordLength(), instructionSet.getConstCordLength());
        programToken = new Parser(program).parse();
        Token lastToken = programToken.getValue()[programToken.getValue().length - 1];
        if (lastToken.getType() == TokenType.ERROR) {
            throw new IllegalArgumentException(lastToken.getValue().toString());
        }
        createGlobalEnvironment();
    }

    /**
     * Create the global environment
     */
    private void createGlobalEnvironment() {
        globalEnvironment = new GlobalEnvironment(programToken, mima, interpreter);
        globalEnvironment.setupGlobalFunctions(MimaInstruction.values());
        if (currentInstructionSet == InstructionSet.MIMA_X) {
            globalEnvironment.setupExtendedInstructionSet();
            globalEnvironment.setupGlobalFunctions(MimaXInstruction.values());
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
     * Start the interpreter
     *
     * @param debug whether the interpreter should pause after each statement
     */
    private void start(boolean debug) {
        if (programToken == null || globalEnvironment == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        interpreter = new Interpreter(currentInstructionSet.getConstCordLength(), threadDebugController, this);
        createGlobalEnvironment();
        sharedException.set(null);
        Thread workingThread = new Thread(() ->
                interpreter.evaluateTopLevel(programToken, globalEnvironment, debug)
        );
        threadDebugController.setWorkingThread(workingThread);
        threadDebugController.start();
    }

    /**
     * Stop the interpreter
     */
    public void stop() {
        interpreter.setRunning(false);
        threadDebugController.stop();
    }

    /**
     * Run the program
     */
    public void run() {
        if (!interpreter.isRunning()) {
            start(false);
        }
        checkForException();
    }

    /**
     * Perform one step of the program
     */
    public void step() {
        if (!interpreter.isRunning()) {
            start(true);
        } else {
            threadDebugController.resume();
        }
        checkForException();
    }

    private void checkForException() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (threadDebugController.isWorking()) {
            Thread.onSpinWait();
        }
        if (sharedException.get() != null) {
            throw new InterpreterException(sharedException.get().getMessage());
        }
    }

    /**
     * Get the current instruction token
     *
     * @return current token
     */
    public Token getCurrentStatement() {
        return interpreter.getCurrentToken();
    }

    /**
     * Get the current mima memory table
     *
     * @return memory table
     */
    public Object[][] getMemoryTable() {
        Environment scope = interpreter.getCurrentScope();
        scope = scope == null || !interpreter.isRunning() ? globalEnvironment : scope;
        Map<String, Integer> map = scope.getDefinitions().get(0).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getValue().toString(), e -> e.getValue().intValue()));
        return mima.memoryTable(map);
    }

    /**
     * Search the parsed program for references and return as List of Sets
     * as follows: {{constant references}, {jump references}, {memory references}}
     *
     * @return list with references sets
     */
    public List<Set<String>> getReferences() {
        return new ReferenceCrawler(programToken).getReferences();
    }

    @Override
    public void notifyException(Exception e) {
        interpreter.setRunning(false);
        sharedException.set(e);
    }
}
