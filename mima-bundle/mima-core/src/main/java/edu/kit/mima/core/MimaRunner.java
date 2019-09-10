package edu.kit.mima.core;

import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SubscriptionService;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * CodeRunner to start/stop execution of Mima Code.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaRunner implements ExceptionHandler, CodeRunner {

    public static final String RUNNING_PROPERTY = "running";
    @NotNull
    private final AtomicReference<Exception> sharedException;
    @NotNull
    private final MimaDebugger debugger;
    @NotNull
    private final SubscriptionService<Boolean> subscriptionService;

    private ThreadDebugController threadDebugController;
    @NotNull
    private Interpreter interpreter;
    @NotNull
    private Mima mima;
    private Program program;
    private GlobalEnvironment globalEnvironment;

    /**
     * Create new MimaRunner.
     */
    public MimaRunner() {
        subscriptionService = new SubscriptionService<>(MimaRunner.class);
        SubscriptionManager.getCurrentManager()
                .offerSubscription(subscriptionService,
                                   RUNNING_PROPERTY, MimaDebugger.PAUSE_PROPERTY, MimaDebugger.RUNNING_PROPERTY);
        debugger = new MimaDebugger();
        interpreter = new Interpreter(0, null, null);
        sharedException = new AtomicReference<>();
        mima = new Mima(InstructionSet.MIMA_X.getWordLength(), InstructionSet.MIMA_X.getConstWordLength());
    }

    /**
     * Start the Execution.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    public void start(final Consumer<Value<?>> callback) {
        mima.reset();
        setupInterpreter(callback);
        threadDebugController.setBreaks(Collections.emptyList());
        threadDebugController.start();
        subscriptionService.notifyEvent(MimaRunner.RUNNING_PROPERTY, true, this);
        do {
            threadDebugController.resume();
            checkForException();
        } while (threadDebugController.isActive() && isRunning());
        subscriptionService.notifyEvent(MimaRunner.RUNNING_PROPERTY, false, this);
    }

    /**
     * Stop the execution.
     */
    public void stop() {
        boolean debuggerWasRunning = debugger.isRunning();
        debugger.active = false;
        interpreter.setRunning(false);
        Optional.ofNullable(threadDebugController).ifPresent(ThreadDebugController::stop);
        if (debugger.active) {
            subscriptionService.notifyEvent(MimaDebugger.RUNNING_PROPERTY, false, debugger);
        }
        subscriptionService.notifyEvent(MimaRunner.RUNNING_PROPERTY, false, this);
        if (debuggerWasRunning) {
            subscriptionService.notifyEvent(Debugger.RUNNING_PROPERTY, false, this);
        }
    }

    /**
     * Create the global environment.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    private void createGlobalEnvironment(final Consumer<Value<?>> callback) {
        final InstructionSet instructionSet = program.getInstructionSet();
        mima = new Mima(instructionSet.getWordLength(), instructionSet.getConstWordLength());
        globalEnvironment = new GlobalEnvironment(program, mima, interpreter, callback);
        globalEnvironment.setupGlobalFunctions(MimaInstruction.values());
        if (instructionSet == InstructionSet.MIMA_X) {
            globalEnvironment.setupExtendedInstructionSet();
            globalEnvironment.setupGlobalFunctions(MimaXInstruction.values());
        }
    }

    /**
     * Start the interpreter.
     *
     * @param callback callback to execute with accumulator after program execution.
     */
    private void setupInterpreter(final Consumer<Value<?>> callback) {
        if (program == null) {
            throw new IllegalStateException("must parse program before starting");
        }
        sharedException.set(null);
        interpreter = new Interpreter(program.getInstructionSet().getConstWordLength(), null, this);
        createGlobalEnvironment(callback);
        threadDebugController = new ThreadDebugController(
                new Thread(() -> interpreter.evaluateTopLevel(program.getProgramToken(), globalEnvironment)),
                () -> {
                    if (debugger.isRunning()) {
                        debugger.pause();
                    }
                });
        interpreter.setDebugController(threadDebugController);
    }

    /**
     * Check if exception has occurred during execution.
     */
    private void checkForException() {
        while (interpreter.isRunning() && threadDebugController.isActive()) {
            Thread.onSpinWait();
        }
        if (sharedException.get() != null) {
            stop();
            throw new RuntimeException(sharedException.get());
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
    public Token<?> getCurrentStatement() {
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
                subscriptionService.notifyEvent(Debugger.PAUSE_PROPERTY, true, this);
            }
            if (!isRunning()) {
                active = false;
                subscriptionService.notifyEvent(Debugger.RUNNING_PROPERTY, false, this);
            }
        }

        @Override
        public void start(final Consumer<Value<?>> callback, @NotNull final Collection<Breakpoint> breakpoints) {
            active = true;
            paused = false;

            subscriptionService.notifyEvent(Debugger.PAUSE_PROPERTY, true, this);

            mima.reset();
            setupInterpreter(callback);
            threadDebugController.setBreaks(breakpoints);
            threadDebugController.start();

            subscriptionService.notifyEvent(Debugger.RUNNING_PROPERTY, true, this);

            continueExecution();
        }

        @Override
        public void pause() {
            paused = true;
            subscriptionService.notifyEvent(Debugger.PAUSE_PROPERTY, true, this);
            threadDebugController.pause();
        }

        @Override
        public void resume() {
            paused = false;
            threadDebugController.setAutoPause(false);
            subscriptionService.notifyEvent(Debugger.PAUSE_PROPERTY, false, this);
            continueExecution();
        }

        @Override
        public void step() {
            paused = true;
            threadDebugController.setAutoPause(true);
            subscriptionService.notifyEvent(Debugger.PAUSE_PROPERTY, true, this);
            continueExecution();
        }

        @Override
        public boolean isRunning() {
            return MimaRunner.this.isRunning() && active;
        }

        @Override
        public boolean isPaused() {
            return active && paused;
        }

    }
}
