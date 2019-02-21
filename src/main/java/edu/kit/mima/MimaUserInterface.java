package edu.kit.mima;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.parsing.ParseReferences;
import edu.kit.mima.core.running.MimaCompiler;
import edu.kit.mima.core.running.MimaRunner;
import edu.kit.mima.core.running.Program;
import edu.kit.mima.gui.button.ButtonPanelBuilder;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.console.LoadingIndicator;
import edu.kit.mima.gui.editor.Editor;
import edu.kit.mima.gui.editor.highlighter.MimaHighlighter;
import edu.kit.mima.gui.loading.FileManager;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.table.FixedScrollTable;
import edu.kit.mima.gui.util.FileName;
import edu.kit.mima.gui.view.MemoryTableView;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * Mima Editor Frame
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUserInterface extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-IDE";

    private static final int HISTORY_CAPACITY = 100;

    private final MimaCompiler mimaCompiler;
    private final MimaRunner mimaRunner;
    private final MemoryTableView memoryView;

    private final FileManager fileManager;
    private final Console console;
    private final FixedScrollTable memoryTable;
    private final Editor editor;

    private final JPanel controlPanel = new JPanel(new BorderLayout());
    private final JButton runButton = new JButton("RUN");
    private final JButton stepButton = new JButton("STEP");
    private final JButton compileButton = new JButton("COMPILE");

    private Thread runThread;

    /**
     * Create a new Mima UI window
     */
    public MimaUserInterface(String filePath) {
        mimaCompiler = new MimaCompiler();
        mimaRunner = new MimaRunner();

        fileManager = new FileManager(this, ParseReferences.MIMA_DIR, ParseReferences.FILE_EXTENSIONS);
        editor = new Editor();
        console = new Console();
        memoryTable = new FixedScrollTable(new String[]{"Address", "Value"}, 100);
        memoryView = new MemoryTableView(mimaRunner, memoryTable);

        Logger.setConsole(console);
        setupWindow();
        setupButtons();
        setupMenu();
        setupEditor();
        setupHotKeys();
        setupComponents();

        restoreSession(filePath);
        memoryView.updateView();
    }

    private void restoreSession(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            updateFile(fileManager::loadLastFile);
        } else {
            updateFile(() -> {
                try {
                    fileManager.load(filePath);
                } catch (IOException e) {
                    fileManager.loadLastFile();
                    Logger.error("Could not load file: " + e.getMessage());
                }
            });
        }
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
        });
        setResizable(true);
        setSize((int) FULLSCREEN.getWidth() / 2, (int) FULLSCREEN.getHeight() / 2);
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
                .addItem("New", () -> updateFile(fileManager::newFile), "control N")
                .addItem("Load", () -> updateFile(fileManager::load), "control L")
                .separator()
                .addItem("Save", this::saveButtonAction, "control S")
                .addItem("Save as", this::saveAs, "control shift S")
                .addItem("Quit", this::quit)
                .addMenu("Edit").setMnemonic('E')
                .addItem("Undo", editor::undo)
                .addItem("Redo", editor::redo)
                .addMenu("View").setMnemonic('V')
                .separator()
                .addItem(binaryView, () -> memoryView.setBinaryView(binaryView.isEnabled()))
                .addMenu("Help").setMnemonic('H')
                .addItem("Show Help", () -> Help.getInstance().setVisible(true))
                .get();
        setJMenuBar(menuBar);
    }

    /**
     * Setup the the action buttons
     */
    private void setupButtons() {
        final JPanel buttonPanel = new ButtonPanelBuilder()
                .addButton(compileButton).addAccelerator("alt C")
                .addAction(this::compileButtonAction).setEnabled(true)
                .addButton(stepButton).addAccelerator("alt S")
                .addAction(this::stepButtonAction).setEnabled(false)
                .addButton(runButton).addAccelerator("alt R")
                .addAction(this::runButtonAction).setEnabled(false)
                .addButton("STOP", this::stopButtonAction, "alt P")
                .get();
        controlPanel.add(buttonPanel, BorderLayout.PAGE_START);
    }

    private void setupComponents() {
        JSplitPane memoryConsole = new JSplitPane();
        memoryConsole.setOrientation(JSplitPane.VERTICAL_SPLIT);
        memoryConsole.setTopComponent(memoryTable);
        memoryConsole.setBottomComponent(console);
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(memoryConsole);
        splitPane.setRightComponent(editor);
        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Setup the editor
     */
    private void setupEditor() {
        MimaHighlighter highlighter = new MimaHighlighter();
        fileManager.addFileEventHandler(highlighter);
        mimaCompiler.addCompilationEventHandler(highlighter);
        editor.setHighlighter(highlighter);

        editor.addEditEventHandler(() -> {
            fileManager.setText(editor.getText().replaceAll(String.format("%n"), "\n"));
            parseFile();
        });

        editor.useStyle(true);
        editor.clean();
        editor.useHistory(true, HISTORY_CAPACITY);
    }

    private void setupHotKeys() {
        KeyListener listener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
                printEventInfo("Key Pressed", event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                printEventInfo("Key Released", event);
            }

            @Override
            public void keyTyped(KeyEvent event) {
                printEventInfo("Key Typed", event);
            }

            private void printEventInfo(String str, KeyEvent e) {
                System.out.println(str);
                int code = e.getKeyCode();
                System.out.println("   Code: " + KeyEvent.getKeyText(code));
                System.out.println("   Char: " + e.getKeyChar());
                int mods = e.getModifiersEx();
                System.out.println("    Mods: "
                        + KeyEvent.getModifiersExText(mods));
                System.out.println("    Location: "
                        + keyboardLocation(e.getKeyLocation()));
                System.out.println("    Action? " + e.isActionKey());
            }
            private String keyboardLocation(int keybrd) {
                switch (keybrd) {
                    case KeyEvent.KEY_LOCATION_RIGHT:
                        return "Right";
                    case KeyEvent.KEY_LOCATION_LEFT:
                        return "Left";
                    case KeyEvent.KEY_LOCATION_NUMPAD:
                        return "NumPad";
                    case KeyEvent.KEY_LOCATION_STANDARD:
                        return "Standard";
                    case KeyEvent.KEY_LOCATION_UNKNOWN:
                    default:
                        return "Unknown";
                }
            }
        };
        editor.addKeyListener(listener);
        editor.setFocusable(true);
    }

    /**
     * Quit the program
     */
    private void quit() {
        try {
            if (fileManager.unsaved()) {
                fileManager.savePopUp();
            }
            fileManager.close();
            dispose();
            Help.close();
            mimaRunner.stop();
        } catch (final IllegalArgumentException | IOException e) {
            Logger.error(e.getMessage());
        }
    }

    private void compileButtonAction() {
        String fileM = "Compiling: \"" + FileName.shorten(fileManager.getLastFile()) + "\"";
        LoadingIndicator.start(fileM, 3);
        try {
            if (mimaRunner.isRunning()) {
                mimaRunner.stop();
            }
            mimaRunner.setProgram(new Program(mimaCompiler.compile(editor.getText()), getInstructionSet()));
            //Update Memory View
            LoadingIndicator.stop(fileM + " (done)");
            runButton.setEnabled(true);
            stepButton.setEnabled(true);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            LoadingIndicator.error("Compilation failed: " + e.getMessage());
            runButton.setEnabled(false);
            stepButton.setEnabled(false);
        }
    }

    private void runButtonAction() {
        runThread = new Thread(() -> {
            stepButton.setEnabled(false);
            runButton.setEnabled(false);
            compileButton.setEnabled(false);
            Logger.log("Running program: " + FileName.shorten(fileManager.getLastFile()));
            LoadingIndicator.start("Running", 3);
            try {
                mimaRunner.run();
                memoryView.updateView();
                stepButton.setEnabled(true);
                runButton.setEnabled(true);
                LoadingIndicator.stop("Running (done)");
            } catch (InterpreterException e) {
                LoadingIndicator.error("Running failed: " + e.getMessage());
                stepButton.setEnabled(false);
                runButton.setEnabled(false);
            } finally {
                compileButton.setEnabled(true);
            }
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
            stepButton.setEnabled(false);
            runButton.setEnabled(false);
        }
    }

    private void stopButtonAction() {
        if (runThread != null && runThread.isAlive()) {
            LoadingIndicator.stop("Running (stopped)");
            runThread.interrupt();
            compileButton.setEnabled(true);
        }
    }

    private void saveButtonAction() {
        try {
            if (!fileManager.isOnDisk()) {
                saveAs();
            } else {
                save();
            }
        } catch (IllegalArgumentException ignored) { }
    }

    /*------------Functionality------------*/

    /**
     * Wrapper function for changing a file
     *
     * @param loadAction function that loads the new file
     */
    private void updateFile(final Runnable loadAction) {
        if (fileManager.unsaved()) {
            fileManager.savePopUp();
        }
        try {
            loadAction.run();
            editor.setText(fileManager.getText());
            afterFileChange();
            console.clear();
            Logger.log("loaded: " + FileName.shorten(fileManager.getLastFile()));
            parseFile();
            editor.resetHistory();
            editor.clean();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Update window title to current file name.
     */
    private void afterFileChange() {
        setTitle(TITLE + ' ' + fileManager.getLastFile().replaceAll(" ", ""));
        ParseReferences.WORKING_DIRECTORY = new File(fileManager.getLastFile()).getParentFile().getAbsolutePath();
    }

    private void parseFile() {
        try {
            mimaCompiler.compile(editor.getText(), false, true, false);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    /*
     * Get the current instruction set
     */
    private InstructionSet getInstructionSet() {
        return fileManager.getLastExtension().equals(ParseReferences.FILE_EXTENSION_X)
                ? InstructionSet.MIMA_X
                : InstructionSet.MIMA;
    }

    /**
     * Save current file
     */
    private void save() {
        try {
            String fileM = "Saving \"" + FileName.shorten(fileManager.getLastFile()) + "\"";
            LoadingIndicator.start(fileM, 3);
            fileManager.save();
            LoadingIndicator.stop(fileM + " (done)");
        } catch (final IllegalArgumentException | IOException e) {
            LoadingIndicator.error("Saving failed: " + e.getMessage());
        }
    }

    /**
     * Save current file and choose file name.
     */
    private void saveAs() {
        fileManager.saveAs();
        afterFileChange();
    }

}
