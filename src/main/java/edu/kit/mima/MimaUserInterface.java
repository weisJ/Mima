package edu.kit.mima;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.running.MimaCompiler;
import edu.kit.mima.core.running.MimaRunner;
import edu.kit.mima.core.running.Program;
import edu.kit.mima.gui.EditorHotKeys;
import edu.kit.mima.gui.components.FixedScrollTable;
import edu.kit.mima.gui.components.ZeroWidthSplitPane;
import edu.kit.mima.gui.components.button.ButtonPanelBuilder;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.gui.components.console.LoadingIndicator;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.editor.highlighter.MimaHighlighter;
import edu.kit.mima.gui.components.folderDisplay.FileDisplay;
import edu.kit.mima.gui.components.tabbedEditor.EditorTabbedPane;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.loading.FileManager;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.menu.settings.Settings;
import edu.kit.mima.gui.util.FileName;
import edu.kit.mima.gui.view.MemoryTableView;
import edu.kit.mima.preferences.MimaConstants;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

/**
 * Mima Editor Frame
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUserInterface extends JFrame implements UserPreferenceChangedListener {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-IDE";

    private final MimaCompiler mimaCompiler;
    private final MimaRunner mimaRunner;
    private final MemoryTableView memoryView;

    private final Map<Editor, FileManager> fileManagers;
    private final Console console;
    private final FixedScrollTable memoryTable;
    private final EditorTabbedPane tabbedEditor;
    private final MimaHighlighter highlighter = new MimaHighlighter();
    private final FileDisplay fileDisplay;
    private final JPanel controlPanel = new JPanel(new BorderLayout());
    private final JButton runButton = new IconButton(Icons.RUN_ACTIVE, Icons.RUN);
    private final JButton stepButton = new JButton("STEP");
    private final JButton stopButton = new IconButton(Icons.STOP_INACTIVE, Icons.STOP);

    private Thread runThread;

    /**
     * Create a new Mima UI window
     */
    public MimaUserInterface(String filePath) {
        mimaCompiler = new MimaCompiler();
        mimaRunner = new MimaRunner();

        fileManagers = new HashMap<>();
        console = new Console();
        tabbedEditor = new EditorTabbedPane();
        memoryTable = new FixedScrollTable(new String[]{"Address", "Value"}, 100);
        memoryView = new MemoryTableView(mimaRunner, memoryTable);
        fileDisplay = new FileDisplay();

        Logger.setConsole(console);
        setupEditor();
        setupFileDisplay();
        startSession(filePath);
        for (var editor : fileManagers.keySet()) {
            editor.setRepaint(true);
            editor.update();
        }
        setupWindow();
        setupButtons();
        setupMenu();
        setupComponents();
        memoryView.updateView();
    }

    private boolean isMimaFile(File file) {
        String name = file.getName();
        return name.endsWith("." + MimaConstants.MIMA_EXTENSION)
                || name.endsWith("." + MimaConstants.MIMA_X_EXTENSION);
    }

    private void startSession(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            String[] files = Preferences.getInstance().readString(PropertyKey.LAST_FILE).split("\"");
            for (String file : files) {
                openFile(file);
            }
        } else {
            openFile(filePath);
        }
    }

    private void openFile(String path) {
        updateFile(fm -> {
            try {
                fm.load(path);
            } catch (IOException e) {
                Logger.error("Could not load file: " + e.getMessage());
            }
        });
    }

    /*------------Window-Setup------------*/

    /**
     * Setup general Frame properties
     */
    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                if (Settings.isOpen()) {
                    Settings.getInstance().toFront();
                    Settings.getInstance().repaint();
                }
            }
        });
        setResizable(true);
        var dimension = new Dimension((int) FULLSCREEN.getWidth() / 2, (int) FULLSCREEN.getHeight() / 2);
        setSize(dimension);
        setPreferredSize(dimension);
        setTitle(TITLE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("images/mima.png")));
    }

    /**
     * Setup the MenuBar with all of its components
     */
    private void setupMenu() {
        JRadioButtonMenuItem binaryView = new JRadioButtonMenuItem("Binary View");
        final JMenuBar menuBar = new MenuBuilder()
                .addMenu("File").setMnemonic('F')
                .addItem("New", () -> updateFile(FileManager::newFile), "control N")
                .addItem("Load", () -> updateFile(FileManager::load), "control L")
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
        setJMenuBar(menuBar);
    }

    /**
     * Setup the the action buttons
     */
    private void setupButtons() {
        final JPanel buttonPanel = new ButtonPanelBuilder(new FlowLayout(FlowLayout.RIGHT, 0, 0))
                .addButton(stepButton).addAccelerator("alt S").setTooltip("Step (Alt+S)")
                .addAction(this::stepButtonAction)
                .addButton(runButton).addAccelerator("alt R").setTooltip("Run (Alt+R)")
                .addAction(this::runButtonAction)
                .addButton(stopButton).addAccelerator("alt P").setTooltip("Stop (Alt+P)")
                .addAction(this::stopButtonAction).setEnabled(false)
                .addSpace()
                .get();
        controlPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void setupFileDisplay() {
        fileDisplay.setHandler(file -> {
            fileDisplay.requestFocus();
            if (file.isDirectory()) {
                fileDisplay.setFile(file);
                fileDisplay.focusLast();
            } else if (isMimaFile(file)) {
                updateFile(fm -> {
                    try {
                        fm.load(file.getAbsolutePath());
                    } catch (IOException e) {
                        Logger.error(e.getMessage());
                    }
                });
            } else {
//                TooltipUtil.showTooltip(new Tooltip("File not supported."));
            }
        });
    }

    private void setupComponents() {
        JSplitPane memoryConsole = new ZeroWidthSplitPane();
        memoryConsole.setOrientation(JSplitPane.VERTICAL_SPLIT);
        memoryConsole.setTopComponent(memoryTable);
        memoryConsole.setBottomComponent(console);
        memoryConsole.setContinuousLayout(true);
        JSplitPane splitPane = new ZeroWidthSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(memoryConsole);
        splitPane.setRightComponent(tabbedEditor);
        controlPanel.add(fileDisplay, BorderLayout.WEST);
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        pack();
        memoryConsole.setDividerLocation(0.5);
        splitPane.setDividerLocation(0.4);
        splitPane.setContinuousLayout(true);
    }

    /**
     * Setup the editor
     */
    private void setupEditor() {
        tabbedEditor.addTabClosedEventHandler(c -> {
            Editor editor = (Editor) c;
            try {
                closeEditor(editor);
            } catch (IOException e) {
                throw new IllegalArgumentException("didn't save");
            }
        });
        tabbedEditor.addChangeListener(e -> {
            Editor editor = (Editor) tabbedEditor.getSelectedComponent();
            fileDisplay.setFile(new File(fileManagers.get(editor).getLastFile()));
        });
    }

    private void closeEditor(Editor editor) throws IOException {
        var fm = fileManagers.get(editor);
        if (fm.unsaved()) {
            fm.savePopUp(() -> {
                throw new IllegalArgumentException("aborted");
            });
        }
        fm.close();
        if (tabbedEditor.getTabCount() <= 1) {
            runButton.setEnabled(false);
            stepButton.setEnabled(false);
        }
    }

    private Editor openEditor() {
        var fm = new FileManager(this, MimaConstants.EXTENSIONS);
        fm.addFileEventHandler(highlighter);
        Editor editor = new Editor();
        fileManagers.put(editor, fm);
        editor.setRepaint(false);
        editor.setHighlighter(highlighter);
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileDisplay.setFile(new File(fm.getLastFile()));
                EditorHotKeys.setEditor(editor);
            }
        });
        editor.addEditEventHandler(() ->
                fm.setText(editor.getText().replaceAll(String.format("%n"), "\n")));
        editor.useStyle(true);
        editor.update();
        editor.useHistory(true, Preferences.getInstance().readInteger(PropertyKey.EDITOR_HISTORY_SIZE));
        editor.showCharacterLimit(80); //Todo Preference
        editor.setText(fm.getText());
        setupHotKeys(editor);
        return editor;
    }

    /**
     * Setup HotKey functionality
     */
    private void setupHotKeys(Editor editor) {
        EditorHotKeys.setEditor(editor);
        for (EditorHotKeys key : EditorHotKeys.values()) {
            editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key.getAccelerator()), key.toString());
            editor.getActionMap().put(key.toString(), key);
        }
    }

    /**
     * Quit the program
     */
    private void quit() {
        try {
            StringBuilder openFiles = new StringBuilder();
            for (var fm : fileManagers.values()) {
                if (fm.unsaved()) {
                    fm.savePopUp(() -> {
                        throw new IllegalArgumentException("aborted");
                    });
                }
                openFiles.append(fm.getLastFile()).append("\"");
                fm.close();
            }
            var pref = Preferences.getInstance();
            pref.saveString(PropertyKey.LAST_FILE, openFiles.toString());
            pref.saveOptions();
            dispose();
            Settings.close();
            Help.close();
            mimaRunner.stop();
            System.exit(0);
        } catch (final IllegalArgumentException ignored) {
        } catch (final IOException e) {
            Logger.error(e.getMessage());
        }
    }

    private void runButtonAction() {
        runThread = new Thread(() -> {
            stepButton.setEnabled(false);
            stopButton.setEnabled(true);
            stopButton.setEnabled(true);
            runButton.setEnabled(false);
            Logger.log("Running program: " + FileName.shorten(currentFileManager().getLastFile()));
            LoadingIndicator.start("Running", 3);
            try {
                mimaRunner.setProgram(new Program(mimaCompiler.compile(currentEditor().getText()), getInstructionSet()));
                memoryView.updateView();
                mimaRunner.run();
                memoryView.updateView();
                stepButton.setEnabled(true);
                runButton.setEnabled(true);
                LoadingIndicator.stop("Running (done)");
            } catch (InterpreterException e) {
                LoadingIndicator.error("Running failed: " + e.getMessage());
                stepButton.setEnabled(false);
                runButton.setEnabled(false);
            }
            stopButton.setEnabled(false);
        });
        runThread.start();
    }

    private void stepButtonAction() {
        try {
            mimaRunner.step();
            if (mimaRunner.getCurrentStatement() != null) {
                Logger.log("Instruction: " + mimaRunner.getCurrentStatement().simpleName());
            }
            runButton.setEnabled(false);
            memoryView.updateView();
            runButton.setEnabled(!mimaRunner.isRunning());
            stepButton.setEnabled(mimaRunner.isRunning());
        } catch (InterpreterException e) {
            Logger.error(e.getMessage());
            runButton.setEnabled(false);
        }
    }

    private void stopButtonAction() {
        if (runThread != null && runThread.isAlive()) {
            LoadingIndicator.stop("Running (stopped)");
            runThread.interrupt();
            runButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void saveButtonAction() {
        try {
            if (!currentFileManager().isOnDisk()) {
                saveAs();
            } else {
                save();
            }
        } catch (IllegalArgumentException ignored) { }
    }

    /*------------Functionality------------*/

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

    private void updateFile(final Consumer<FileManager> loadAction) {
        updateFile(loadAction, true);
    }

    /**
     * Wrapper function for changing a file
     *
     * @param loadAction function that loads the new file
     */
    private void updateFile(final Consumer<FileManager> loadAction, boolean createNew) {
        try {
            Editor editor;
            if (createNew) {
                editor = openEditor();
            } else {
                editor = currentEditor();
            }
            var fm = fileManagers.get(editor);
            loadAction.accept(fm);
            for (var entry : fileManagers.entrySet()) {
                if (entry.getValue() != fm
                        && entry.getValue().getLastFile().equals(fm.getLastFile())) {
                    tabbedEditor.setSelectedComponent(entry.getKey());
                    return;
                }
            }
            editor.setText(fm.getText());
            String title = fm.getLastFile();
            title = title.substring(Math.max(Math.min(title.lastIndexOf('\\') + 1, title.length() - 1), 0));
            tabbedEditor.addTab(title, Icons.foFile(title), editor);
            afterFileChange();
            console.clear();
            Logger.log("loaded: " + FileName.shorten(fm.getLastFile()));
            editor.setRepaint(true);
            editor.resetHistory();
            editor.update();
        } catch (final IllegalArgumentException ignored) {
        } catch (final IllegalStateException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Update window title to current file name.
     */
    private void afterFileChange() {
        String file = currentFileManager().getLastFile();
        setTitle(TITLE + ' ' + FileName.shorten(file));
        File parent = new File(currentFileManager().getLastFile()).getParentFile();
        var pref = Preferences.getInstance();
        String workDir = parent != null
                ? parent.getAbsolutePath()
                : pref.readString(PropertyKey.DIRECTORY_MIMA);
        pref.saveString(PropertyKey.DIRECTORY_WORKING, workDir);
        pref.saveString(PropertyKey.LAST_FILE, currentFileManager().getLastFile());
    }

    /*
     * Get the current instruction set
     */
    private InstructionSet getInstructionSet() {
        return currentFileManager().getLastExtension().equals(MimaConstants.MIMA_X_EXTENSION)
                ? InstructionSet.MIMA_X
                : InstructionSet.MIMA;
    }

    /**
     * Save current file
     */
    private void save() {
        try {
            String fileM = "Saving \"" + FileName.shorten(currentFileManager().getLastFile()) + "\"";
            LoadingIndicator.start(fileM, 3);
            currentFileManager().save();
            LoadingIndicator.stop(fileM + " (done)");
        } catch (final IllegalArgumentException | IOException e) {
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
    public void notifyUserPreferenceChanged(PropertyKey key) {
        if (key == PropertyKey.EDITOR_HISTORY_SIZE
                || key == PropertyKey.EDITOR_HISTORY) {
            var pref = Preferences.getInstance();
            for (var editor : fileManagers.keySet()) {
                editor.useHistory(pref.readBoolean(PropertyKey.EDITOR_HISTORY),
                        pref.readInteger(PropertyKey.EDITOR_HISTORY_SIZE));
            }
        }
    }
}
