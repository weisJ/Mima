package edu.kit.mima.core;

import edu.kit.mima.api.observing.AbstractObservable;
import edu.kit.mima.core.controller.ThreadDebugController;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.core.interpretation.ExceptionHandler;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.interpretation.environment.GlobalEnvironment;
import edu.kit.mima.core.token.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * CodeRunner to start/stop execution of Mima Code.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRunner extends AbstractObservable implements ExceptionHandler, CodeRunner {

    public static final String RUNNING_PROPERTY = "running";
    @NotNull private final AtomicReference<Exception> sharedException;
    @NotNull private final ThreadDebugController threadDebugController;
    @NotNull private final MimaDebugger debugger;

    @NotNull private Interpreter interpreter;
    @NotNull private Mima mima;
    private Program program;
    private GlobalEnvironment globalEnvironment;

    /**
     * Create new MimaRunner.
     */
    public MimaRunner() {
        debugger = new MimaDebugger();
        interpreter = new Interpreter(0, null, null);
        sharedException = new AtomicReference<>();
        threadDebugController = new ThreadDebugController();
        mima = new Mima(InstructionSet.MIMA_X.getWordLength(),
                        InstructionSet.MIMA_X.getConstCordLength());
    }

    /**
     * Start the Execution.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    public void start(final Consumer<Value> callback) {
        threadDebugController.setBreaks(Collections.emptyList());
        mima.reset();
        startInterpreter(callback);
        do {
            threadDebugController.resume();
            checkForException();
        } while (threadDebugController.isActive() && isRunning());
        getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, true, false);
    }

    /**
     * Start the interpreter.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    private void startInterpreter(final Consumer<Value> callback) {
        if (program == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        interpreter = new Interpreter(program.getInstructionSet().getConstCordLength(),
                                      threadDebugController,
                                      this);
        createGlobalEnvironment(callback);
        sharedException.set(null);
        final Thread workingThread = new Thread(
                () -> interpreter.evaluateTopLevel(program.getProgramToken(),
                                                   globalEnvironment)
        );
        threadDebugController.setWorkingThread(workingThread);
        threadDebugController.start();
        interpreter.setRunning(true);
        getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, false, true);
    }

    /**
     * Create the global environment.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    private void createGlobalEnvironment(final Consumer<Value> callback) {
        final InstructionSet instructionSet = program.getInstructionSet();
        mima = new Mima(instructionSet.getWordLength(), instructionSet.getConstCordLength());
        globalEnvironment = new GlobalEnvironment(program.getProgramToken(), mima,
                                                  interpreter, callback);
        globalEnvironment.setupGlobalFunctions(MimaInstruction.values());
        if (instructionSet == InstructionSet.MIMA_X) {
            globalEnvironment.setupExtendedInstructionSet();
            globalEnvironment.setupGlobalFunctions(MimaXInstruction.values());
        }
    }

    /**
     * Stop the execution.
     */
    public void stop() {
        final boolean running = isRunning();
        debugger.active = false;
        threadDebugController.stop();
        getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, running, false);
    }

    /**
     * Check if exception has occurred during execution.
     */
    private void checkForException() {
        while (interpreter.isRunning() && threadDebugController.isActive()) {
            Thread.onSpinWait();
        }
        if (sharedException.get() != null) {
            MimaCoreDefaults.getLogger().error(sharedException.get().getMessage());
            stop();
        }
    }

    @Override
    public void notifyException(final Exception e) {
        sharedException.set(e);
        threadDebugController.stop();
    }

    /**
     * Set the program to evaluate.
     *
     * @param program program to evaluate.
     */
    public void setProgram(final Program program) {
        if (isRunning()) {
            throw new MimaRuntimeException("Can't change program during execution");
        }
        this.program = program;
        this.mima.reset();
    }

    /**
     * Get the current instruction token.
     *
     * @return current token
     */
    @Nullable
    public Token getCurrentStatement() {
        return interpreter.getCurrentToken();
    }

    /**
     * Get the current environment of execution.
     *
     * @return current environment if running (see {@link #isRunning}), global environment else.
     */
    @Nullable
    public Environment getCurrentEnvironment() {
        if (!isRunning()) {
            return globalEnvironment;
        }
        final var scope = interpreter.getCurrentScope();
        return scope == null ? globalEnvironment : scope;
    }

    @NotNull
    public Mima getMima() {
        return mima;
    }

    /**
     * Returns whether the underlying interpreter is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return interpreter.isRunning();
    }

    /**
     * Get the debugger for this MimaRunner.
     *
     * @return Debugger instance
     */
    @NotNull
    public Debugger debugger() {
        return debugger;
    }

    private class MimaDebugger implements Debugger {
        private boolean active = false;
        private boolean paused = false;

        private void continueExecution() {
            do {
                threadDebugController.resume();
                checkForException();
            } while (threadDebugController.isActive() && isRunning());
            if (!threadDebugController.isActive()) {
                paused = true;
                getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, false, true);
            }
            if (!isRunning()) {
                active = false;
                getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, true, false);
            }
        }

        @Override
        public void start(final Consumer<Value> callback) {
            active = true;
            paused = false;
            getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, false, true);
            mima.reset();
            startInterpreter(callback);
            continueExecution();
        }

        @Override
        public void pause() {
            paused = true;
            threadDebugController.pause();
            getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, false, true);
        }

        @Override
        public void resume() {
            paused = false;
            threadDebugController.setAutoPause(false);
            continueExecution();
        }

        @Override
        public void step() {
            paused = false;
            threadDebugController.setAutoPause(true);
            threadDebugController.resume();
            getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, true, false);
            checkForException();
        }

        @Override
        public boolean isRunning() {
            return MimaRunner.this.isRunning() && active;
        }

        @Override
        public boolean isPaused() {
            return active && paused;
        }

        @Override
        public void setBreakpoints(@NotNull final Breakpoint[] breakpoints) {
            threadDebugController.setBreaks(Arrays.stream(breakpoints)
                                                    .map(Breakpoint::getLineIndex)
                                                    .collect(Collectors.toList()));
        }

        @Override
        public void addPropertyChangeListener(final String property,
                                              final PropertyChangeListener listener) {
            MimaRunner.this.addPropertyChangeListener(property, listener);
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            MimaRunner.this.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(final String property,
                                                 final PropertyChangeListener listener) {
            MimaRunner.this.removePropertyChangeListener(property, listener);
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            MimaRunner.this.removePropertyChangeListener(listener);
        }
    }
}
