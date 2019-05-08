package edu.kit.mima.app;

import edu.kit.mima.App;
import edu.kit.mima.core.Debugger;
import edu.kit.mima.core.MimaCompiler;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.core.MimaRuntimeException;
import edu.kit.mima.core.Program;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.logger.LoadingIndicator;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.Contract;

/**
 * Actions for mima App.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class RunActions {

    private final MimaUserInterface mimaUI;
    private final MimaCompiler mimaCompiler;
    private final MimaRunner mimaRunner;
    private final Debugger debugger;
    private Thread runThread;

    /**
     * Create new actions for mima App.
     *
     * @param mimaUI       parent app.
     * @param mimaCompiler the compiler of app.
     * @param mimaRunner   runner of app.
     * @param debugger     the debugger of app.
     */
    @Contract(pure = true)
    public RunActions(
            final MimaUserInterface mimaUI,
            final MimaCompiler mimaCompiler,
            final MimaRunner mimaRunner,
            final Debugger debugger) {
        this.mimaUI = mimaUI;
        this.mimaRunner = mimaRunner;
        this.debugger = debugger;
        this.mimaCompiler = mimaCompiler;
    }

    /**
     * Stop code execution.
     */
    public void stop() {
        if (runThread != null) {
            LoadingIndicator.stop("Running (stopped)");
            mimaRunner.stop();
            runThread.interrupt();
        }
    }

    /**
     * Get the debugger.
     *
     * @return the debugger.
     */
    public Debugger getDebugger() {
        return debugger;
    }

    /**
     * Get the runner.
     *
     * @return the runner.
     */
    public MimaRunner getMimaRunner() {
        return mimaRunner;
    }

    private void executionAction(final boolean debug) {
        runThread =
                new Thread(
                        () -> {
                            String file = mimaUI.currentFileManager().getLastFile();
                            App.logger.log("Executing program: " + FileName.shorten(file));
                            LoadingIndicator.start("Executing", 3);
                            var pref = Preferences.getInstance();
                            mimaRunner.setProgram(
                                    new Program(
                                            mimaCompiler.compile(
                                                    mimaUI.currentEditor().getText(),
                                                    mimaUI.currentFileManager().getLastFile(),
                                                    pref.readString(PropertyKey.DIRECTORY_WORKING),
                                                    pref.readString(PropertyKey.DIRECTORY_MIMA)),
                                            MimaConstants.instructionSetForFile(file)));
                            if (debug) {
                                debugger.start(
                                        v -> LoadingIndicator.stop("Executing (done)"),
                                        mimaUI.currentEditor().getBreakpoints());
                            } else {
                                mimaRunner.start(v -> LoadingIndicator.stop("Executing (done)"));
                            }
                        });
        final Thread.UncaughtExceptionHandler exceptionHandler =
                (t, e) -> {
                    if (e instanceof MimaRuntimeException || e instanceof InterpreterException) {
                        LoadingIndicator.error("Execution " + "failed: " + e.getMessage());
                    } else {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                };
        /*
         * Register exception handler for the AWT event thread and for the execution thread.
         */
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
        runThread.setUncaughtExceptionHandler(exceptionHandler);
        runThread.start();
    }

    /**
     * Start code execution.
     */
    public void run() {
        executionAction(false);
    }

    /**
     * Start code debugger.
     */
    public void debug() {
        executionAction(true);
    }
}
