package edu.kit.mima;

import edu.kit.mima.api.history.History;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.running.Debugger;
import edu.kit.mima.core.running.MimaCompiler;
import edu.kit.mima.core.running.MimaRunner;
import edu.kit.mima.core.running.MimaRuntimeException;
import edu.kit.mima.core.running.Program;
import edu.kit.mima.gui.EditorHotKeys;
import edu.kit.mima.gui.components.FixedScrollTable;
import edu.kit.mima.gui.components.ZeroWidthSplitPane;
import edu.kit.mima.gui.components.button.ButtonPanelBuilder;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.button.RunnableIconButton;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.editor.highlighter.MimaHighlighter;
import edu.kit.mima.gui.components.folderdisplay.FileDisplay;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.menu.settings.Settings;
import edu.kit.mima.gui.view.MemoryTableView;
import edu.kit.mima.loading.FileManager;
import edu.kit.mima.logging.LoadingIndicator;
import edu.kit.mima.logging.Logger;
import edu.kit.mima.preferences.MimaConstants;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import edu.kit.mima.util.BindingUtil;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Mima Editor Frame.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUserInterface extends JFrame implements UserPreferenceChangedListener {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-IDE";

    @NotNull private final MimaCompiler mimaCompiler;
    @NotNull private final MimaRunner mimaRunner;
    @NotNull private final Debugger debugger;
    @NotNull private final MemoryTableView memoryView;

    @NotNull private final Map<Editor, FileManager> fileManagers;
    @NotNull private final Console console;
    @NotNull private final FixedScrollTable memoryTable;
    @NotNull private final EditorTabbedPane tabbedEditor;
    private final MimaHighlighter highlighter = new MimaHighlighter();
    @NotNull private final FileDisplay fileDisplay;
    private final JPanel controlPanel = new JPanel(new BorderLayout());

    private Thread runThread;

    /**
     * Create a new Mima UI window.
     *
     * @param filePath path of file to open
     */
    public MimaUserInterface(@Nullable final String filePath) {
        mimaCompiler = new MimaCompiler();
        mimaRunner = new MimaRunner();
        debugger = mimaRunner.debugger();

        fileManagers = new HashMap<>();
        console = new Console();
        tabbedEditor = new EditorTabbedPane();
        memoryTable = new FixedScrollTable(new String[]{"Address", "Value"}, 100);
        memoryView = new MemoryTableView(mimaRunner, memoryTable);
        fileDisplay = new FileDisplay();
        BindingUtil.bind(debugger, () -> {
            currentEditor().markLine(mimaRunner.getCurrentStatement().getFilePos());
            memoryView.updateView();
        }, Debugger.PAUSE_PROPERTY);

        Logger.setConsole(console);
        setupEditorPane();
        setupFileDisplay();
        startSession(filePath);
        for (final var editor : fileManagers.keySet()) {
            editor.setRepaint(true);
            editor.update();
        }
        setupWindow();
        setupButtons();
        setJMenuBar(createMenu());
        setupComponents();
        memoryView.updateView();
    }

    private boolean isMimaFile(@NotNull final File file) {
        final String name = file.getName();
        return name.endsWith("." + MimaConstants.MIMA_EXTENSION)
                || name.endsWith("." + MimaConstants.MIMA_X_EXTENSION);
    }

    private void startSession(@Nullable final String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            final String filesString = Preferences.getInstance().readString(PropertyKey.LAST_FILE);
            if (filesString.length() < 2) {
                return;
            }
            final String[] files = filesString.substring(1).split("\"");
            for (final String file : files) {
                openFile(file);
            }
        } else {
            openFile(filePath);
        }
    }

    /**
     * Setup general Frame properties.
     */
    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                quit();
            }

            @Override
            public void windowActivated(final WindowEvent e) {
                if (Settings.isOpen()) {
                    Settings.getInstance().toFront();
                    Settings.getInstance().repaint();
                }
            }
        });
        setResizable(true);
        final var dimension = new Dimension((int) FULLSCREEN.getWidth() / 2,
                                            (int) FULLSCREEN.getHeight() / 2);
        setSize(dimension);
        setPreferredSize(dimension);
        setTitle(TITLE);
        setIconImage(
                Toolkit.getDefaultToolkit()
                        .getImage(getClass().getClassLoader().getResource("images/mima.png")));
    }

    /**
     * Setup the MenuBar with all of its components.
     */
    @NotNull
    private JMenuBar createMenu() {
        final JRadioButtonMenuItem binaryView = new JRadioButtonMenuItem("Binary View");
        //@formatter:off
        final var menu = new MenuBuilder()
                .addMenu("File").setMnemonic('F')
                    .addItem("New", () -> openFile(FileManager::newFile), "control N")
                    .addItem("Load", () -> openFile(FileManager::load), "control L")
                    .separator()
                    .addItem("Settings", () -> Settings.showWindow(this), "control alt S")
                    .separator()
                    .addItem("Save", this::saveButtonAction, "control S")
                    .addItem("Save as", this::saveAs, "control shift S")
                    .addItem("Quit", this::quit)
                .addMenu("Edit").setMnemonic('E')
                    .addItem("Undo", currentEditor()::undo)
                    .addItem("Redo", currentEditor()::redo)
                .addMenu("View").setMnemonic('V')
                    .separator()
                    .addItem(binaryView, () -> memoryView.setBinaryView(binaryView.isEnabled()))
                .addMenu("Help").setMnemonic('H')
                    .addItem("Show Help", () -> Help.showWindow(this))
                .get();
        //@formatter:on
        menu.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("Border.light")));
        return menu;
    }

    /**
     * Setup the the action buttons.
     */
    @SuppressWarnings("CheckStyle")
    private void setupButtons() {
        final RunnableIconButton runButton = new RunnableIconButton(Icons.RUN_INACTIVE,
                                                                    Icons.RUN,
                                                                    Icons.RUN_ACTIVE);
        final RunnableIconButton debugButton = new RunnableIconButton(Icons.DEBUG_INACTIVE,
                                                                      Icons.DEBUG,
                                                                      Icons.DEBUG_ACTIVE);
        // @formatter:off
        final JPanel buttonPanel = new ButtonPanelBuilder()
                .addButton(new IconButton(Icons.PAUSE_INACTIVE, Icons.PAUSE))
                    .bindVisible(debugger, debugger::isRunning, Debugger.RUNNING_PROPERTY)
                    .bindEnabled(debugger, () -> !debugger.isPaused(), Debugger.PAUSE_PROPERTY)
                    .addAction(debugger::pause)
                    .setVisible(false)
                .addButton(new IconButton(Icons.RESUME_INACTIVE, Icons.RESUME))
                    .addAction(debugger::resume)
                    .bindEnabled(debugger, debugger::isPaused, Debugger.PAUSE_PROPERTY)
                    .bindVisible(debugger, debugger::isRunning, Debugger.RUNNING_PROPERTY)
                    .setVisible(false)
                .addButton(new IconButton(Icons.REDO_INACTIVE, Icons.REDO))
                    .addAccelerator("alt S").setTooltip("Step (Alt+S)")
                    .addAction(debugger::step)
                    .bindEnabled(debugger, debugger::isPaused, Debugger.PAUSE_PROPERTY)
                    .bindVisible(debugger, debugger::isRunning, Debugger.RUNNING_PROPERTY)
                    .setVisible(false)
                .addSeparator()
                    .bindVisible(debugger, debugger::isRunning, Debugger.RUNNING_PROPERTY)
                    .setVisible(false)
                .addButton(debugButton)
                    .addAction(this::debugButtonAction)
                    .bindEnabled(debugger, () -> !debugger.isRunning(), Debugger.RUNNING_PROPERTY)
                    .bind(debugger, () -> debugButton.setRunning(debugger.isRunning()), Debugger.RUNNING_PROPERTY)
                .addButton(runButton).addAccelerator("alt R").setTooltip("Run (Alt+R)")
                    .addAction(this::runButtonAction)
                    .bindEnabled(mimaRunner, () -> !mimaRunner.isRunning(), MimaRunner.RUNNING_PROPERTY)
                    .bind(debugger, () -> runButton.setRunning(!debugger.isRunning() && mimaRunner.isRunning()), Debugger.RUNNING_PROPERTY)
                .addButton(new IconButton(Icons.STOP_INACTIVE, Icons.STOP)).addAccelerator("alt P").setTooltip("Stop (Alt+P)")
                    .addAction(this::stopButtonAction).setEnabled(false)
                    .bindEnabled(mimaRunner, mimaRunner::isRunning, MimaRunner.RUNNING_PROPERTY)
                .addSpace()
                .addButton(new IconButton(Icons.UNDO_INACTIVE, Icons.UNDO))
                    .addAction(() -> currentEditor().undo()).setTooltip("Redo (Ctrl+Z)")
                    .bindClassEnabled(History.class, () -> currentEditor().canUndo(), History.LENGTH_PROPERTY, History.POSITION_PROPERTY)
                    .bindEnabled(tabbedEditor, () -> currentEditor().canUndo(), EditorTabbedPane.SELECTED_TAB_PROPERTY)
                    .setEnabled(false)
                .addButton(new IconButton(Icons.REDO_INACTIVE, Icons.REDO))
                    .addAction(() -> currentEditor().redo()).setTooltip("Redo (Ctrl+Shift+Z)")
                    .bindClassEnabled(History.class,() -> currentEditor().canRedo(), History.LENGTH_PROPERTY, History.POSITION_PROPERTY)
                    .bindEnabled(tabbedEditor, () -> currentEditor().canRedo(), EditorTabbedPane.SELECTED_TAB_PROPERTY)
                    .setEnabled(false)
                .addSpace()
                .get();
        // @formatter:on
        controlPanel.add(buttonPanel, BorderLayout.EAST);
    }

    /*
     * Create FileDisplay behaviour
     */
    private void setupFileDisplay() {
        fileDisplay.setHandler(file -> {
            fileDisplay.requestFocus();
            if (file.isDirectory()) {
                fileDisplay.setFile(file);
                fileDisplay.focusLast();
            } else if (isMimaFile(file)) {
                openFile(fm -> {
                    try {
                        fm.load(file.getAbsolutePath());
                    } catch (@NotNull final IOException e) {
                        Logger.error(e.getMessage());
                    }
                });
            }
        });
    }


    /**
     * Setup the editor.
     */
    private void setupEditorPane() {
        tabbedEditor.addTabClosedEventHandler(c -> {
            final Editor editor = (Editor) c;
            try {
                closeEditor(editor);
            } catch (@NotNull final IOException e) {
                throw new IllegalArgumentException("didn't save");
            }
        });
        tabbedEditor.addChangeListener(e -> {
            final Editor editor = (Editor) tabbedEditor.getSelectedComponent();
            if (editor == null) {
                return;
            }
            fileDisplay.setFile(new File(fileManagers.get(editor).getLastFile()));
        });
    }

    private void setupComponents() {
        final JSplitPane memoryConsole = new ZeroWidthSplitPane();
        memoryConsole.setOrientation(JSplitPane.VERTICAL_SPLIT);
        memoryConsole.setTopComponent(memoryTable);
        memoryConsole.setBottomComponent(console);
        memoryConsole.setContinuousLayout(true);
        final JSplitPane splitPane = new ZeroWidthSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(memoryConsole);
        splitPane.setRightComponent(tabbedEditor);
        controlPanel.add(fileDisplay, BorderLayout.WEST);
        controlPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0,
                                UIManager.getColor("Border.light")),
                new EmptyBorder(2, 2, 2, 2)));
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        pack();
        memoryConsole.setDividerLocation(0.5);
        splitPane.setDividerLocation(0.4);
        splitPane.setContinuousLayout(true);
    }

    private void closeEditor(@NotNull final Editor editor) throws IOException {
        final var fm = fileManagers.get(editor);
        if (fm.unsaved()) {
            fm.savePopUp(() -> {
                throw new IllegalArgumentException("aborted");
            });
        }
        fm.close();
        editor.close();
        fileManagers.remove(editor).getLastFile();
        if (tabbedEditor.getTabCount() <= 1) {
            //Todo disable button if no file is selected.
        }
    }

    @NotNull
    private Editor openEditor() {
        final var fm = new FileManager(this, MimaConstants.EXTENSIONS);
        fm.addFileEventHandler(highlighter);
        final Editor editor = new Editor();
        fileManagers.put(editor, fm);
        editor.setRepaint(false);
        editor.setHighlighter(highlighter);
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                fileDisplay.setFile(new File(fm.getLastFile()));
                EditorHotKeys.setEditor(editor);
            }
        });
        editor.addEditEventHandler(() -> fm
                .setText(editor.getText().replaceAll(String.format("%n"), "\n")));
        editor.useStyle(true);
        editor.update();
        editor.useHistory(true,
                          Preferences.getInstance().readInteger(PropertyKey.EDITOR_HISTORY_SIZE));
        editor.showCharacterLimit(80); //Todo Preference
        editor.setText(fm.getText());
        setupHotKeys(editor);
        return editor;
    }

    /**
     * Setup HotKey functionality.
     */
    private void setupHotKeys(@NotNull final Editor editor) {
        EditorHotKeys.setEditor(editor);
        for (final EditorHotKeys key : EditorHotKeys.values()) {
            editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key.getAccelerator()), key.toString());
            editor.getActionMap().put(key.toString(), key);
        }
    }

    /**
     * Quit the program.
     */
    private void quit() {
        try {
            final StringBuilder openFiles = new StringBuilder("\"");
            for (final var fm : fileManagers.values()) {
                if (fm.unsaved()) {
                    fm.savePopUp(() -> {
                        throw new IllegalArgumentException("aborted");
                    });
                }
                openFiles.append(fm.getLastFile()).append("\"");
                fm.close();
            }
            final var pref = Preferences.getInstance();
            pref.saveString(PropertyKey.LAST_FILE, openFiles.toString());
            pref.saveOptions();
            dispose();
            Settings.close();
            Help.close();
            mimaRunner.stop();
            System.exit(0);
        } catch (@NotNull final IllegalArgumentException ignored) {
        } catch (@NotNull final IOException e) {
            Logger.error(e.getMessage());
        }
    }

    private void runButtonAction() {
        executionAction(false);
    }

    private void debugButtonAction() {
        executionAction(true);
    }

    private void executionAction(final boolean debug) {
        runThread = new Thread(() -> {
            Logger.log("Executing program: "
                               + FileName.shorten(currentFileManager().getLastFile()));
            LoadingIndicator.start("Executing", 3);
            try {
                mimaRunner.setProgram(new Program(mimaCompiler.compile(currentEditor().getText()),
                                                  getInstructionSet()));
                memoryView.updateView();
                if (debug) {
                    debugger.setBreakpoints(currentEditor().getBreakpoints());
                    debugger.start(v -> {
                        currentEditor().markLine(-1);
                        LoadingIndicator.stop("Executing (done)");
                    });
                } else {
                    mimaRunner.start(v -> LoadingIndicator.stop("Executing (done)"));
                }
                memoryView.updateView();
            } catch (@NotNull final InterpreterException e) {
                LoadingIndicator.error("Execution failed: " + e.getMessage());
            } catch (@NotNull final MimaRuntimeException ignored) {
            }
        });
        runThread.start();
    }

    private void stopButtonAction() {
        if (runThread != null) {
            LoadingIndicator.stop("Running (stopped)");
            if (debugger.isRunning()) {
                currentEditor().markLine(-1);
            }
            mimaRunner.stop();
            runThread.interrupt();
        }
    }

    private void saveButtonAction() {
        try {
            if (!currentFileManager().isOnDisk()) {
                saveAs();
            } else {
                save();
            }
        } catch (@NotNull final IllegalArgumentException ignored) {
        }
    }

    private Editor currentEditor() {
        if (fileManagers.size() <= 1) {
            return fileManagers.keySet().stream().findFirst().orElse(null);
        } else {
            return (Editor) tabbedEditor.getComponentAt(tabbedEditor.getSelectedIndex());
        }
    }

    private FileManager currentFileManager() {
        if (fileManagers.size() <= 1) {
            return fileManagers.values().stream().findFirst().orElse(null);
        } else {
            return fileManagers.get(currentEditor());
        }
    }

    private void openFile(@NotNull final String path) {
        openFile(fm -> {
            try {
                fm.load(path);
            } catch (@NotNull final IOException e) {
                Logger.error("Could not load file: " + e.getMessage());
            }
        });
    }

    /**
     * Wrapper function for opening a file.
     *
     * @param loadAction function that loads the new file
     */
    private void openFile(@NotNull final Consumer<FileManager> loadAction) {
        try {
            final Editor editor = openEditor();
            final var fm = fileManagers.get(editor);
            loadAction.accept(fm);
            for (final var entry : fileManagers.entrySet()) {
                if (entry.getValue() != fm
                        && entry.getValue().getLastFile().equals(fm.getLastFile())) {
                    fileManagers.remove(editor);
                    tabbedEditor.setSelectedComponent(entry.getKey());
                    return;
                }
            }
            editor.setText(fm.getText());
            String title = fm.getLastFile();
            title = title.substring(Math.max(Math.min(title.lastIndexOf('\\') + 1,
                                                      title.length() - 1), 0));
            tabbedEditor.addTab(title, Icons.forFile(title), editor);
            afterFileChange();
            console.clear();

            editor.setRepaint(true);
            editor.resetHistory();
            editor.update();
            Logger.log("loaded: " + FileName.shorten(fm.getLastFile()));
        } catch (@NotNull final IllegalArgumentException ignored) {
        } catch (@NotNull final IllegalStateException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Update window title to current file name.
     */
    private void afterFileChange() {
        final String file = currentFileManager().getLastFile();
        setTitle(TITLE + ' ' + FileName.shorten(file));
        final File parent = new File(currentFileManager().getLastFile()).getParentFile();
        final var pref = Preferences.getInstance();
        final String workDir = parent != null
                ? parent.getAbsolutePath()
                : pref.readString(PropertyKey.DIRECTORY_MIMA);
        pref.saveString(PropertyKey.DIRECTORY_WORKING, workDir);
        pref.saveString(PropertyKey.LAST_FILE, currentFileManager().getLastFile());
    }

    /**
     * Get the currently used instruction Set.
     *
     * @return the current instruction set
     */
    @NotNull
    private InstructionSet getInstructionSet() {
        return currentFileManager().getLastExtension().equals(MimaConstants.MIMA_X_EXTENSION)
                ? InstructionSet.MIMA_X
                : InstructionSet.MIMA;
    }

    /**
     * Save current file.
     */
    private void save() {
        try {
            String fileM = "Saving \""
                    + FileName.shorten(currentFileManager().getLastFile()) + "\"";
            LoadingIndicator.start(fileM, 3);
            currentFileManager().save();
            LoadingIndicator.stop(fileM + " (done)");
        } catch (@NotNull final IllegalArgumentException | IOException e) {
            LoadingIndicator.error("Saving failed: " + e.getMessage());
        }
    }

    /**
     * Save current file and choose file name.
     */
    private void saveAs() {
        currentFileManager().saveAs();
        afterFileChange();
    }

    @Override
    public void notifyUserPreferenceChanged(final PropertyKey key) {
        if (key == PropertyKey.EDITOR_HISTORY_SIZE
                || key == PropertyKey.EDITOR_HISTORY) {
            final var pref = Preferences.getInstance();
            for (final var editor : fileManagers.keySet()) {
                editor.useHistory(pref.readBoolean(PropertyKey.EDITOR_HISTORY),
                                  pref.readInteger(PropertyKey.EDITOR_HISTORY_SIZE));
            }
        }
    }
}
