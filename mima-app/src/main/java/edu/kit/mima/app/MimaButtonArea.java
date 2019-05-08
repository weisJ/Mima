package edu.kit.mima.app;

import edu.kit.mima.api.event.SimpleSubscriber;
import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.history.History;
import edu.kit.mima.core.Debugger;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.gui.components.button.ButtonPanelBuilder;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.button.RunnableIconButton;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Button Area for Mima App.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaButtonArea {

    private final MimaUserInterface mimaUI;
    private final RunActions runActions;
    @NotNull
    private final JPanel panel;

    /**
     * Button area for Mima App.
     *
     * @param mimaUI  parent app.
     * @param actions actions for running.
     */
    public MimaButtonArea(final MimaUserInterface mimaUI, final RunActions actions) {
        this.runActions = actions;
        this.mimaUI = mimaUI;
        this.panel = createButtons();
    }

    @NotNull
    private JPanel createButtons() {
        final RunnableIconButton runButton = new RunnableIconButton(Icons.RUN_INACTIVE, Icons.RUN,
                Icons.RUN_ACTIVE);
        final RunnableIconButton debugButton = new RunnableIconButton(Icons.DEBUG_INACTIVE, Icons.DEBUG,
                Icons.DEBUG_ACTIVE);
        var debugger = runActions.getDebugger();
        var mimaRunner = runActions.getMimaRunner();
        var sm = SubscriptionManager.getCurrentManager();
        var undo = new IconButton(Icons.UNDO_INACTIVE, Icons.UNDO);
        var redo = new IconButton(Icons.REDO_INACTIVE, Icons.REDO);
        sm.subscribe(new SimpleSubscriber(((identifier, value) -> {
                    var editor = mimaUI.currentEditor();
                    undo.setEnabled(editor != null && editor.canUndo());
                })), History.POSITION_PROPERTY, History.LENGTH_PROPERTY,
                EditorTabbedPane.SELECTED_TAB_PROPERTY);
        sm.subscribe(new SimpleSubscriber(((identifier, value) -> {
                    var editor = mimaUI.currentEditor();
                    redo.setEnabled(editor != null && editor.canRedo());
                })), History.POSITION_PROPERTY, History.LENGTH_PROPERTY,
                EditorTabbedPane.SELECTED_TAB_PROPERTY);
        return new ButtonPanelBuilder()
                       // Pause
                       .addButton(new IconButton(Icons.PAUSE_INACTIVE, Icons.PAUSE)).bindVisible(
                        debugger, debugger::isRunning, Debugger.RUNNING_PROPERTY).bindEnabled(debugger,
                        () -> !debugger.isPaused(),
                        Debugger.PAUSE_PROPERTY).addAction(
                        debugger::pause).setVisible(false)
                       // Resume
                       .addButton(new IconButton(Icons.RESUME_INACTIVE, Icons.RESUME)).addAction(
                        debugger::resume).bindEnabled(debugger, debugger::isPaused,
                        Debugger.PAUSE_PROPERTY).bindVisible(debugger,
                        debugger::isRunning,
                        Debugger.RUNNING_PROPERTY).setVisible(
                        false)
                       // Step
                       .addButton(new IconButton(Icons.REDO_INACTIVE, Icons.REDO)).addAccelerator(
                        "alt S").setTooltip("Step (Alt+S)").addAction(debugger::step).bindEnabled(
                        debugger, debugger::isPaused, Debugger.PAUSE_PROPERTY).bindVisible(debugger,
                        debugger::isRunning,
                        Debugger.RUNNING_PROPERTY).setVisible(
                        false)
                       // Separator
                       .addSeparator().bindVisible(debugger, debugger::isRunning,
                        Debugger.RUNNING_PROPERTY).setVisible(false)
                       // Debug
                       .addButton(debugButton).addAccelerator("alt D").setTooltip(
                        "Debug (Alt+D)").addAction(runActions::debug).bindEnabled(debugger,
                        () -> !debugger.isRunning(),
                        Debugger.RUNNING_PROPERTY).bind(
                        debugger, () -> debugButton.setRunning(debugger.isRunning()),
                        Debugger.RUNNING_PROPERTY)
                       // Run
                       .addButton(runButton).addAccelerator("alt R").setTooltip(
                        "Run (Alt+R)").addAction(runActions::run).bindEnabled(mimaRunner,
                        () -> !mimaRunner.isRunning(),
                        MimaRunner.RUNNING_PROPERTY).bind(
                        debugger,
                        () -> runButton.setRunning(!debugger.isRunning() && mimaRunner.isRunning()),
                        Debugger.RUNNING_PROPERTY)
                       // Stop
                       .addButton(new IconButton(Icons.STOP_INACTIVE, Icons.STOP)).addAccelerator(
                        "alt P").setTooltip("Stop (Alt+P)").addAction(runActions::stop).setEnabled(
                        false).bindEnabled(mimaRunner, mimaRunner::isRunning,
                        MimaRunner.RUNNING_PROPERTY).addSpace()
                       // Undo
                       .addButton(undo).addAction(() -> mimaUI.currentEditor().undo()).setTooltip(
                        "Redo (Ctrl+Z)").setEnabled(false)
                       // Redo
                       .addButton(redo).addAction(() -> mimaUI.currentEditor().redo()).setTooltip(
                        "Redo (Ctrl+Shift+Z)").setEnabled(false).addSpace().get();
    }

    /**
     * Get the panel.
     *
     * @return panel with buttons.
     */
    @NotNull
    public JPanel getPane() {
        return panel;
    }
}
