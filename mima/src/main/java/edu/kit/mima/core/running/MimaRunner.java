package edu.kit.mima.core.running;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.controller.ThreadDebugController;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.instruction.MimaInstruction;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.interpretation.ExceptionListener;
import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.interpretation.environment.GlobalEnvironment;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.gui.components.Breakpoint;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.observing.AbstractObservable;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRunner extends AbstractObservable implements ExceptionListener {

    public static final String RUNNING_PROPERTY = "running";
    private final static Environment EMPTY_ENV = new Environment(null, new ProgramToken(new Token[0], -1));
    private final AtomicReference<Exception> sharedException;
    private final ThreadDebugController threadDebugController;
    private final MimaDebugger debugger;

    private Interpreter interpreter;
    private Mima mima;
    private Program program;
    private GlobalEnvironment globalEnvironment;


    public MimaRunner() {
        debugger = new MimaDebugger();
        interpreter = new Interpreter(0, null, null);
        sharedException = new AtomicReference<>();
        threadDebugController = new ThreadDebugController();
        mima = new Mima(InstructionSet.MIMA_X.getWordLength(), InstructionSet.MIMA_X.getConstCordLength());
    }

    public void start(Consumer<Value> callback) {
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
     * Start the interpreter
     */
    private void startInterpreter(Consumer<Value> callback) {
        if (program == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        interpreter = new Interpreter(program.getInstructionSet().getConstCordLength(), threadDebugController, this);
        createGlobalEnvironment();
        sharedException.set(null);
        Thread workingThread = new Thread(
                () -> {
                    interpreter.evaluateTopLevel(program.getProgramToken(), globalEnvironment, v -> {});
                    callback.accept(new Value<>(ValueType.NUMBER, mima.getAccumulator()));
                }
        );
        threadDebugController.setWorkingThread(workingThread);
        threadDebugController.start();
        interpreter.setRunning(true);
        getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, false, true);
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
        boolean running = isRunning();
        debugger.active = false;
        threadDebugController.stop();
        getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, running, false);
    }

    private void checkForException() {
        while (interpreter.isRunning() && threadDebugController.isActive()) {
            Thread.onSpinWait();
        }
        if (sharedException.get() != null) {
            Logger.error(sharedException.get().getMessage());
            stop();
        }
    }

    @Override
    public void notifyException(Exception e) {
        sharedException.set(e);
        threadDebugController.stop();
    }

    /**
     * Set the program to evaluate.
     *
     * @param program program to evaluate.
     */
    public void setProgram(Program program) {
        if (isRunning()) {
            throw new MimaRuntimeException("Can't change program during execution");
        }
        this.program = program;
        this.mima.reset();
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
        if (!isRunning()) {
            return globalEnvironment;
        }
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

    public Debugger debugger() {
        return debugger;
    }

    private class MimaDebugger implements Debugger {
        private final List<PauseListener> listenerList = new ArrayList<>();
        private boolean active = false;
        private boolean paused = false;

        private void continueExecution() {
            do {
                threadDebugController.resume();
                checkForException();
            } while (threadDebugController.isActive() && isRunning());
            if (!threadDebugController.isActive()) {
                paused = true;
                notifyListeners(getCurrentStatement());
                getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, false, true);
            }
            if (!isRunning()) {
                active = false;
                getPropertyChangeSupport().firePropertyChange(RUNNING_PROPERTY, true, false);
            }
        }

        @Override
        public void start(Consumer<Value> callback) {
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
            notifyListeners(getCurrentStatement());
            getPropertyChangeSupport().firePropertyChange(Debugger.PAUSE_PROPERTY, false, true);
        }

        private void notifyListeners(Token currentStatement) {
            for (var listener : listenerList) {
                if (listener != null) {
                    listener.paused(currentStatement);
                }
            }
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
            notifyListeners(getCurrentStatement());
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
        public void setBreakpoints(Breakpoint[] breakpoints) {
            threadDebugController.setBreaks(Arrays.stream(breakpoints)
                    .map(Breakpoint::getLineIndex).collect(Collectors.toList()));
        }

        @Override
        public void addPauseListener(PauseListener listener) {
            listenerList.add(listener);
        }

        @Override
        public void removePauseListener(PauseListener listener) {
            listenerList.remove(listener);
        }

        @Override
        public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
            MimaRunner.this.addPropertyChangeListener(property, listener);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            MimaRunner.this.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
            MimaRunner.this.removePropertyChangeListener(property, listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            MimaRunner.this.removePropertyChangeListener(listener);
        }
    }
}
