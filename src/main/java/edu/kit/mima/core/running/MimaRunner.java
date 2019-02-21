package edu.kit.mima.core.running;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.controller.ThreadDebugController;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.ExceptionListener;
import edu.kit.mima.core.interpretation.GlobalEnvironment;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.parsing.token.Token;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRunner implements ExceptionListener {

    private final AtomicReference<Exception> sharedException;
    private final ThreadDebugController threadDebugController;

    private Interpreter interpreter;
    private Mima mima;
    private Program program;
    private GlobalEnvironment globalEnvironment;


    public MimaRunner() {
        interpreter = new Interpreter(0, null, null);
        sharedException = new AtomicReference<>();
        threadDebugController = new ThreadDebugController();
        threadDebugController.addStopHandler(() -> interpreter.setRunning(false));
        mima = new Mima(InstructionSet.MIMA_X.getWordLength(), InstructionSet.MIMA_X.getConstCordLength());
    }

    /**
     * Start the interpreter
     */
    private void start() {
        if (program == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        interpreter = new Interpreter(program.getInstructionSet().getConstCordLength(), threadDebugController, this);
        createGlobalEnvironment();
        sharedException.set(null);
        Thread workingThread = new Thread(
                () -> interpreter.evaluateTopLevel(program.getProgramToken(), globalEnvironment)
        );
        threadDebugController.setWorkingThread(workingThread);
        threadDebugController.start();
        interpreter.setRunning(true);
    }

    /**
     * Create the global environment
     */
    private void createGlobalEnvironment() {
        InstructionSet instructionSet = program.getInstructionSet();
        mima = new Mima(instructionSet.getWordLength(), instructionSet.getConstCordLength());
        globalEnvironment = new GlobalEnvironment(program.getProgramToken(), mima, interpreter);
        globalEnvironment.setupGlobalFunctions(MimaInstruction.values());
        if (instructionSet == InstructionSet.MIMA_X) {
            globalEnvironment.setupExtendedInstructionSet();
            globalEnvironment.setupGlobalFunctions(MimaXInstruction.values());
        }
    }

    /**
     * Stop the interpreter
     */
    public void stop() {
        threadDebugController.stop();
    }

    /**
     * Run the program
     */
    public void run() {
        mima.reset();
        do {
            step();
        } while (interpreter.isRunning());
    }

    /**
     * Perform one step of the program
     */
    public void step() {
        if (!interpreter.isRunning()) {
            start();
        } else if (!threadDebugController.isActive()) {
            threadDebugController.resume();
        }
        checkForException();
    }

    private void checkForException() {
        while (interpreter.isRunning() && threadDebugController.isActive()) {
            Thread.onSpinWait();
        }
        if (sharedException.get() != null) {
            throw new InterpreterException(sharedException.get().getMessage());
        }
    }

    @Override
    public void notifyException(Exception e) {
        sharedException.set(e);
        threadDebugController.stop();
    }

    public void setProgram(Program program) {
        if (isRunning()) {
            throw new MimaRuntimeException("Can't change program during execution");
        }
        this.program = program;
    }

    /**
     * Get the current instruction token
     *
     * @return current token
     */
    public Token getCurrentStatement() {
        return interpreter.getCurrentToken();
    }

    public Environment getCurrentEnvironment() {
        var scope = interpreter.getCurrentScope();
        return scope == null ? globalEnvironment : scope;
    }

    public Mima getMima() {
        return mima;
    }

    /**
     * Returns whether the underlying interpreter is running
     *
     * @return true if running
     */
    public boolean isRunning() {
        return interpreter.isRunning();
    }
}
