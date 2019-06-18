package edu.kit.mima.app;

import edu.kit.mima.api.event.SimpleSubscriber;
import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.history.History;
import edu.kit.mima.core.Debugger;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.gui.components.button.ButtonPanelBuilder;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.button.RunnableIconButton;
import edu.kit.mima.gui.components.tabbedpane.EditorTabbedPane;
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
    private final RunnableIconButton runButton;
    private final RunnableIconButton debugButton;
    private final IconButton pause;
    private final IconButton resume;
    private final IconButton undo;
    private final IconButton redo;
    private final IconButton step;
    private final IconButton stop;
    private final ButtonPanelBuilder.Separator separator;
    private final Debugger debugger;
    private final MimaRunner mimaRunner;

    /**
     * Button area for Mima App.
     *
     * @param mimaUI  parent app.
     * @param actions actions for running.
     */
    public MimaButtonArea(final MimaUserInterface mimaUI, final RunActions actions) {
        this.runActions = actions;
        this.mimaUI = mimaUI;
        runButton = new RunnableIconButton(Icons.RUN_INACTIVE, Icons.RUN,
                                           Icons.RUN_ACTIVE);
        debugButton = new RunnableIconButton(Icons.DEBUG_INACTIVE, Icons.DEBUG,
                                             Icons.DEBUG_ACTIVE);
        pause = new IconButton(Icons.PAUSE_INACTIVE, Icons.PAUSE);
        resume = new IconButton(Icons.RESUME_INACTIVE, Icons.RESUME);
        undo = new IconButton(Icons.UNDO_INACTIVE, Icons.UNDO);
        redo = new IconButton(Icons.REDO_INACTIVE, Icons.REDO);
        step = new IconButton(Icons.REDO_INACTIVE, Icons.REDO);
        stop = new IconButton(Icons.STOP_INACTIVE, Icons.STOP);
        separator = ButtonPanelBuilder.createSeparator();
        debugger = runActions.getDebugger();
        mimaRunner = runActions.getMimaRunner();
        this.panel = createButtons();
    }

    private void createSubscriptions() {
        final var sm = SubscriptionManager.getCurrentManager();

        sm.subscribe(new SimpleSubscriber<>((identifier, value) -> {
                         var editor = mimaUI.currentEditor();
                         redo.setEnabled(editor != null && editor.canRedo());
                         undo.setEnabled(editor != null && editor.canUndo());
                     }),
                     History.POSITION_PROPERTY,
                     History.LENGTH_PROPERTY,
                     EditorTabbedPane.SELECTED_TAB_PROPERTY);
        sm.subscribe(new SimpleSubscriber<Boolean>((identifier, value) -> {
                         pause.setVisible(value);
                         resume.setVisible(value);
                         step.setVisible(value);
                         debugButton.setEnabled(!value);
                         debugButton.setRunning(value);
                         runButton.setEnabled(!value);
                         runButton.setRunning(!value && mimaRunner.isRunning());
                         separator.setVisible(value);
                     }),
                     Debugger.RUNNING_PROPERTY);
        sm.subscribe(new SimpleSubscriber<Boolean>((identifier, value) -> {
                         pause.setEnabled(!value);
                         resume.setEnabled(value);
                         step.setEnabled(value);
                     }),
                     Debugger.PAUSE_PROPERTY);
        sm.subscribe(new SimpleSubscriber<Boolean>((identifier, value) -> {
                         runButton.setEnabled(!value);
                         stop.setEnabled(value);
                     }),
                     MimaRunner.RUNNING_PROPERTY);
    }

    @NotNull
    private JPanel createButtons() {
        createSubscriptions();
        return new ButtonPanelBuilder()
                       // Pause
                       .addButton(pause)
                       .addAccelerator("F2").setTooltip("Pause (F2)")
                       .addAction(debugger::pause).setVisible(false)
                       // Resume
                       .addButton(resume)
                       .addAccelerator("F1").setTooltip("Resume (F1)")
                       .addAction(debugger::resume).setVisible(false)
                       // Step
                       .addButton(step)
                       .addAccelerator("F3").setTooltip("Step (F3)")
                       .addAction(debugger::step).setVisible(false)
                       // Separator
                       .addSeparator(separator).setVisible(false)
                       // Debug
                       .addButton(debugButton)
                       .addAccelerator("F4").setTooltip("Debug (F4)")
                       .addAction(runActions::debug)
                       // Run
                       .addButton(runButton)
                       .addAccelerator("F5").setTooltip("Run (F5)")
                       .addAction(runActions::run)
                       // Stop
                       .addButton(stop).addAccelerator("F6").setTooltip("Stop (F6)")
                       .addAction(runActions::stop).setEnabled(false)
                       .addSpace()
                       // Undo
                       .addButton(undo).setTooltip("Redo (Ctrl+Z)")
                       .addAction(() -> mimaUI.currentEditor().undo()).setEnabled(false)
                       // Redo
                       .addButton(redo).setTooltip("Redo (Ctrl+Shift+Z)")
                       .addAction(() -> mimaUI.currentEditor().redo()).setEnabled(false)
                       .addSpace().get();
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
