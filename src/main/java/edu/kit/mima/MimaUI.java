package edu.kit.mima;

import edu.kit.mima.core.*;
import edu.kit.mima.core.parsing.*;
import edu.kit.mima.gui.*;
import edu.kit.mima.gui.button.*;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.editor.*;
import edu.kit.mima.gui.logging.*;
import edu.kit.mima.gui.menu.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;

import static edu.kit.mima.gui.logging.Logger.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUI extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-Simulator";
    private static final String FILE_EXTENSION = "mima";
    private static final String FILE_EXTENSION_X = "mimax";
    private static final String MIMA_DIR = System.getProperty("user.home") + "/.mima";

    private final Mima mima;
    private final FileManager fileManager;
    private final Console console;
    private final FixedScrollTable memoryView;

    private final Editor editor;
    private final StyleGroup syntaxStyle;
    private final StyleGroup referenceStyle;

    private final JButton run = new JButton("RUN");
    private final JButton step = new JButton("STEP");

    /**
     * Create a new Mima UI window
     */
    private MimaUI() {
        super();
        fileManager = new FileManager(this, MIMA_DIR, new String[]{FILE_EXTENSION, FILE_EXTENSION_X});
        editor = new Editor();
        console = new Console();
        memoryView = new FixedScrollTable(new String[]{"Address", "Value"});
        Logger.setConsole(console);

        setupWindow();
        final JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        add(memoryConsole, BorderLayout.LINE_START);
        setupButtons();
        setupMenu();
        add(editor, BorderLayout.CENTER);

        mima = new Mima();
        fileManager.loadLastFile();
        editor.setText(fileManager.getText());
        reloadMima();

        syntaxStyle = new StyleGroup();
        referenceStyle = new StyleGroup();
        editor.addStyleGroup(syntaxStyle);
        editor.addStyleGroup(referenceStyle);
        editor.useTabs(true);
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
        editor.addAfterEditAction(() -> fileManager.setText(editor.getText()));
        editor.addAfterEditAction(this::updateReferenceHighlighting);

        editor.useStyle(true);
        editor.clean();
        editor.useHistory(true, 100);
    }

    /**
     * Entry point for starting the Mima UI
     *
     * @param args command line arguments (ignored)
     */
    public static void main(final String[] args) {
//        try {
//            UIManager.setLookAndFeel(WebLookAndFeel.class.getCanonicalName());
//        } catch (ClassNotFoundException | IllegalAccessException
//                | InstantiationException | UnsupportedLookAndFeelException e) {
//            e.printStackTrace();
//        }
        final MimaUI frame = new MimaUI();
        frame.setVisible(true);
        frame.repaint();
    }

    //////////////////////////////////////////////////////////////////////////
    ///                         Window Setup                               ///
    //////////////////////////////////////////////////////////////////////////

    /**
     * Setup general Frame properties
     */
    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(setupWindowListener());
        setResizable(true);
        setSize((int) FULLSCREEN.getWidth() / 2, (int) FULLSCREEN.getHeight() / 2);
        setTitle(TITLE);
        setLayout(new BorderLayout());
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("mima.png")));
    }

    /**
     * Setup the MenuBar with all of its components
     */
    private void setupMenu() {
        // @formatter:off
        final JMenuBar menuBar = new MenuBuilder()
                .addMenu("Help")
                        .addItem("Show Help", () -> Help.getInstance().setVisible(true))
                .addMenu("File")
                        .addItem("New", () -> changeFile(fileManager::newFile), "control N")
                        .addItem("Load", () -> changeFile(fileManager::load), "control L")
                        .separator()
                        .addItem("Save", () -> {
                            if (!fileManager.isOnDisk()) fileManager.saveAs();
                            else save();
                        }, "control S")
                        .addItem("Save as", fileManager::saveAs, "control shift S")
                .addMenu("Edit")
                        .addItem("Undo", editor::undo, "control Z")
                        .addItem("Redo", editor::redo, "control shift Z")
                .get();
        setJMenuBar(menuBar);
        // @formatter:on
    }

    /**
     * Setup the the action buttons
     */
    private void setupButtons() {
        // @formatter:off
        final JPanel buttonPanel = new ButtonPanelFactory()
                .addButton("COMPILE", this::compile, "alt R")
                .addButton("RESET", this::reset, "alt shift R")
                .addButton(step).addAccelerator("alt S").addAction(this::step).setEnabled(false)
                .addButton(run).addAccelerator("alt R").addAction(this::run).setEnabled(false)
                .get();
        add(buttonPanel, BorderLayout.PAGE_START);
        // @formatter:on
        add(buttonPanel, BorderLayout.PAGE_START);
    }

    /**
     * Create the Window Listener for the frame
     *
     * @return the WindowListener
     */
    private WindowListener setupWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                try {
                    if (!fileManager.isSaved()) fileManager.savePopUp();
                    fileManager.close();
                    e.getWindow().dispose();
                    Help.close();
                } catch (final IllegalArgumentException | IOException ignored) { }
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////||
    ///                         Functionality                              /||
    ////////////////////////////////////////////////////////////////////////||

    /**
     * Wrapper function for changing a file
     *
     * @param loadAction function that loads the new file
     */
    private void changeFile(final Runnable loadAction) {
        if (!fileManager.isSaved()) fileManager.savePopUp();
        console.clear();
        loadAction.run();
        editor.setText(fileManager.getText());
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
        editor.resetHistory();
        editor.clean();
    }

    /**
     * Perform one instruction of the mima.
     */
    private void step() {
        try {
            log("Step!");
            mima.step();
            run.setEnabled(false);
            log("Instruction: " + fileManager.lines()[mima.getCurrentLineIndex() - 1]);
            updateMemoryTable();
            step.setEnabled(mima.isRunning());
        } catch (final IllegalArgumentException | IllegalStateException e) {
            error(e.getMessage());
            step.setEnabled(false);
            run.setEnabled(false);
        }
    }

    /**
     * Run all instructions of the mima
     */
    private void run() {
        step.setEnabled(false);
        run.setEnabled(false);
        log("Running program: " + fileManager.getLastFile() + "...");
        try {
            mima.run();
            updateMemoryTable();
        } catch (final IllegalArgumentException | IllegalStateException e) {
            error(e.getMessage());
            reset();
        }
        step.setEnabled(true);
        run.setEnabled(true);
        log("done");
    }

    /**
     * Reset the mima to begin of program.
     */
    private void reset() {
        mima.reset();
        run.setEnabled(true);
        step.setEnabled(true);
        updateMemoryTable();
    }

    /**
     * Load new program into mima
     */
    private void reloadMima() {
        try {
            mima.loadProgram(fileManager.lines().clone(), fileManager.getLastExtension().equals(FILE_EXTENSION_X));
            run.setEnabled(false);
            step.setEnabled(false);
        } catch (final IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    /**
     * Save current file
     */
    private void save() {
        try {
            log("saving...");
            fileManager.save();
            log("done");
        } catch (final IOException e) {
            error("failed to save: " + e.getMessage());
        }
    }

    /**
     * Compile mima program
     */
    private void compile() {
        log("Compiling: " + fileManager.getLastFile() + "...");
        try {
            reloadMima();
            updateMemoryTable();
            run.setEnabled(true);
            step.setEnabled(true);
        } catch (final IllegalArgumentException e) {
            error(e.getMessage());
            run.setEnabled(false);
            step.setEnabled(false);
        }
        log("done");
    }

    /**
     * Update the Memory table with new values
     */
    private void updateMemoryTable() {
        memoryView.setContent(mima.memoryTable());
        repaint();
    }

    /**
     * Update the syntax highlighting according to the current instruction set
     */
    private void updateSyntaxHighlighting() {
        final Color instructionsColor = new Color(27, 115, 207);
        final Color keywordColor = new Color(168, 120, 43);

        syntaxStyle.setHighlight(Interpreter.getKeywords(), new Color[]{
                keywordColor, //$define
                keywordColor, //const
                keywordColor, // :
                keywordColor, // (
                keywordColor, // )
                new Color(37, 143, 148), //Numbers
                new Color(165, 170, 56), //0b,
                new Color(63, 135, 54), //Comments
        });
        if (fileManager.getLastExtension().equals(FILE_EXTENSION_X)) {
            syntaxStyle.addHighlight(Mima.getMimaXInstructionSet(), instructionsColor);
        }
    }

    /**
     * Perform code analysis to fetch current associations for syntax highlighting
     * Performs a silent compile on the instructions
     */
    private void updateReferenceHighlighting() {
        if (mima == null) return;
        try {
            final List<Set<String>> references = mima.getReferences(fileManager.lines().clone());

            referenceStyle.setHighlight(Mima.getInstructionSet(), new Color(27, 115, 207));
            final String[] constants = references.get(0)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(constants, new Color(255, 96, 179));
            final String[] instructionReferences = references.get(2)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(instructionReferences, new Color(63, 135, 54));
            final String[] memoryReferences = references.get(1)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(memoryReferences, new Color(136, 37, 170));
        } catch (final IllegalArgumentException ignored) { }
    }

}
