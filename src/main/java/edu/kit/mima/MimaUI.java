package edu.kit.mima;

import edu.kit.mima.core.controller.InstructionSet;
import edu.kit.mima.core.controller.MimaController;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.button.ButtonPanelBuilder;
import edu.kit.mima.gui.color.SyntaxColor;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.editor.Editor;
import edu.kit.mima.gui.editor.style.StyleGroup;
import edu.kit.mima.gui.loading.FileManager;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.table.FixedScrollTable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Mima Editor Frame
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUI extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-Simulator";
    private static final String FILE_EXTENSION = "mima";
    private static final String FILE_EXTENSION_X = "mimax";
    private static final String MIMA_DIR = System.getProperty("user.home") + "/.mima";

    private static final int HISTORY_CAPACITY = 100;

    private final MimaController controller;

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
        controller = new MimaController();
        fileManager = new FileManager(this, MIMA_DIR, new String[]{FILE_EXTENSION, FILE_EXTENSION_X});
        editor = new Editor();
        console = new Console();
        memoryView = new FixedScrollTable(new String[]{"Address", "Value"});
        syntaxStyle = new StyleGroup();
        referenceStyle = new StyleGroup();
        Logger.setConsole(console);

        setupWindow();
        final JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        add(memoryConsole, BorderLayout.LINE_START);
        setupButtons();
        setupMenu();
        add(editor, BorderLayout.CENTER);

        updateFile(fileManager::loadLastFile);

        editor.addStyleGroup(syntaxStyle);
        editor.addStyleGroup(referenceStyle);
        editor.addAfterEditAction(() -> fileManager.setText(editor.getText()));
        editor.addAfterEditAction(() -> {
            /* no need to error while writing*/
            try { parse(editor.getText()); } catch (IllegalArgumentException | IllegalStateException ignored) { }
        });
        editor.addAfterEditAction(this::updateReferenceHighlighting);
        editor.useStyle(true);
        editor.clean();
        editor.useHistory(true, HISTORY_CAPACITY);

        updateSyntaxHighlighting();
        updateReferenceHighlighting();
    }

    /**
     * Entry point for starting the Mima UI
     *
     * @param args command line arguments (ignored)
     */
    public static void main(final String[] args) {
        final MimaUI frame = new MimaUI();
        Logger.setLevel(Logger.LogLevel.INFO);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.repaint();
    }

    /*------------Window-Setup------------*/

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
        final JMenuBar menuBar = new MenuBuilder()
                .addMenu("File").setMnemonic('F')
                .addItem("New", () -> updateFile(fileManager::newFile), "control N")
                .addItem("Load", () -> updateFile(fileManager::load), "control L")
                .separator()
                .addItem("Save", () -> {
                    if (!fileManager.isOnDisk()) {
                        fileManager.saveAs();
                    } else {
                        save();
                    }
                }, "control S")
                .addItem("Save as", fileManager::saveAs, "control shift S")
                .addItem("Quit", this::quit)
                .addMenu("Edit").setMnemonic('E')
                .addItem("Undo", editor::undo, "control Z")
                .addItem("Redo", editor::redo, "control shift Z")
                .addMenu("View").setMnemonic('V')
                .addItem("Zoom In", () -> editor.setFontSize(editor.getFontSize() + 1), "control PLUS")
                .addItem("Zoom Out", () -> editor.setFontSize(editor.getFontSize() - 1), "control MINUS")
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
                .addButton("COMPILE", this::compile, "alt C")
                .addButton(step).addAccelerator("alt S").addAction(this::step).setEnabled(false)
                .addButton(run).addAccelerator("alt R").addAction(this::run).setEnabled(false)
                .get();
        add(buttonPanel, BorderLayout.PAGE_START);
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
                quit();
            }
        };
    }

    /**
     * Quit the program
     */
    private void quit() {
        try {
            if (!fileManager.isSaved()) {
                fileManager.savePopUp();
            }
            fileManager.close();
            dispose();
            Help.close();
            controller.stop();
        } catch (final IllegalArgumentException | IOException e) {
            Logger.error(e.getMessage());
        }
    }

    /*------------Functionality------------*/


    /**
     * Wrapper function for changing a file
     *
     * @param loadAction function that loads the new file
     */
    private void updateFile(final Runnable loadAction) {
        if (!fileManager.isSaved()) {
            fileManager.savePopUp();
        }
        console.clear();
        loadAction.run();
        String text = fileManager.getText();
        editor.setText(text);
        try {
            parse(text);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Logger.error(e.getMessage());
        }
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
        editor.resetHistory();
        editor.clean();
    }

    /*
     * Parse the current text file
     */
    private void parse(String text) {
        InstructionSet instructionSet = fileManager.getLastExtension().equals(FILE_EXTENSION_X)
                ? InstructionSet.MIMA_X
                : InstructionSet.MIMA;
        controller.parse(text, instructionSet);
    }

    /**
     * Perform one instruction of the mima.
     */
    private void step() {
        try {
            Logger.log("Step!");
            controller.step();
            run.setEnabled(false);
            if (controller.getCurrent() != null) {
                Logger.log("Instruction: " + controller.getCurrent().simpleName());
            }
            updateMemoryTable();
            run.setEnabled(!controller.isRunning());
        } catch (final InterpreterException e) {
            Logger.error(e.getMessage());
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
        Logger.log("Running program: " + fileManager.getLastFile() + "...");
        try {
            controller.run();
            updateMemoryTable();
            step.setEnabled(true);
            run.setEnabled(true);
            Logger.log("done");
        } catch (final InterpreterException e) {
            Logger.error(e.getMessage());
            step.setEnabled(false);
            run.setEnabled(false);
        }
    }

    /**
     * Save current file
     */
    private void save() {
        try {
            Logger.log("saving...");
            fileManager.save();
            Logger.log("done");
        } catch (final IOException e) {
            Logger.error("failed to save: " + e.getMessage());
        }
    }

    /**
     * Compile mima program
     */
    private void compile() {
        Logger.log("Compiling: " + fileManager.getLastFile() + "...");
        try {
            parse(editor.getText());
            updateMemoryTable();
            run.setEnabled(true);
            step.setEnabled(true);
            Logger.log("done");
        } catch (final IllegalArgumentException | IllegalStateException e) {
            Logger.error(e.getMessage());
            run.setEnabled(false);
            step.setEnabled(false);
        }
    }

    /**
     * Update the MemoryMap table with new values
     */
    private void updateMemoryTable() {
        memoryView.setContent(controller.getMemoryTable());
        repaint();
    }

    /**
     * Update the syntax highlighting according to the current instruction set
     */
    private void updateSyntaxHighlighting() {
        syntaxStyle.setHighlight(Keyword.getKeywords(), SyntaxColor.KEYWORD);

        syntaxStyle.addHighlight('\\' + String.valueOf(Punctuation.OPEN_BRACKET), SyntaxColor.TEXT);
        syntaxStyle.addHighlight('\\' + String.valueOf(Punctuation.CLOSED_BRACKET), SyntaxColor.TEXT);
        syntaxStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_OPEN), SyntaxColor.TEXT);
        syntaxStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_CLOSED), SyntaxColor.TEXT);
        syntaxStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_BEGIN), SyntaxColor.KEYWORD);
        syntaxStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD);
        syntaxStyle.addHighlight(String.valueOf(Punctuation.INSTRUCTION_END), SyntaxColor.KEYWORD);
        syntaxStyle.addHighlight(String.valueOf(Punctuation.COMMA), SyntaxColor.KEYWORD);

        syntaxStyle.addHighlight("-?[0-9]+", SyntaxColor.NUMBER);
        syntaxStyle.addHighlight(Punctuation.BINARY_PREFIX + "[10]*", SyntaxColor.BINARY);
        syntaxStyle.addHighlight(controller.getInstructionSet().getInstructions(), SyntaxColor.INSTRUCTION);
        syntaxStyle.addHighlight("HALT", SyntaxColor.WARNING);
        syntaxStyle.addHighlight(Punctuation.COMMENT + "[^\n" + Punctuation.COMMENT + "]*" + Punctuation.COMMENT + '?',
                SyntaxColor.COMMENT);
    }

    /**
     * Perform code analysis to fetch current associations for syntax highlighting
     * Performs a silent compile on the instructions
     */
    private void updateReferenceHighlighting() {
        try {
            final List<Set<String>> references = controller.getReferences();

            final String[] constants = references.get(0)
                    .stream().map(s -> "(?<=[\\s\\(,])(\\s)*" + s + "(\\s)*(?=[\\),:;])").toArray(String[]::new);
            referenceStyle.addHighlight(constants, SyntaxColor.CONSTANT);
            final String[] jumpReferences = references.get(1)
                    .stream().map(s -> "(?<=[\\s\\(,])(\\s)*" + s + "(\\s)*(?=[\\),:;])").toArray(String[]::new);
            referenceStyle.addHighlight(jumpReferences, SyntaxColor.JUMP);
            final String[] memoryReferences = references.get(2)
                    .stream().map(s -> "(?<=[\\s\\(,])(\\s)*" + s + "(\\s)*(?=[\\),:;])").toArray(String[]::new);
            referenceStyle.addHighlight(memoryReferences, SyntaxColor.REFERENCE);
        } catch (final IllegalArgumentException e) {
            Logger.error(e.getMessage());
        }
    }

}
