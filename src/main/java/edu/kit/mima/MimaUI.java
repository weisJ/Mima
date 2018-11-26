package edu.kit.mima;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.controller.MimaController;
import edu.kit.mima.core.interpretation.InterpreterException;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.button.ButtonPanelBuilder;
import edu.kit.mima.gui.color.SyntaxColor;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.editor.Editor;
import edu.kit.mima.gui.editor.style.StyleGroup;
import edu.kit.mima.gui.editor.view.HighlightView;
import edu.kit.mima.gui.loading.FileManager;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.MenuBuilder;
import edu.kit.mima.gui.table.FixedScrollTable;
import edu.kit.mima.gui.util.FileName;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
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
    private static final String MIMA_DIR = System.getProperty("user.home") + "\\.mima";

    private static final int HISTORY_CAPACITY = 100;
    private static final int MAX_FILE_DISPLAY_LENGTH = 45;

    private final MimaController controller;

    private final FileManager fileManager;
    private final Console console;
    private final FixedScrollTable memoryView;

    private final Editor editor;
    private final StyleGroup syntaxStyle;
    private final StyleGroup referenceStyle;

    private final JPanel controlPanel = new JPanel(new BorderLayout());
    private final JButton run = new JButton("RUN");
    private final JButton step = new JButton("STEP");
    private final JRadioButtonMenuItem binaryView = new JRadioButtonMenuItem("Binary View");

    /**
     * Create a new Mima UI window
     */
    public MimaUI(String filePath) {
        controller = new MimaController();
        fileManager = new FileManager(this, MIMA_DIR, new String[]{FILE_EXTENSION, FILE_EXTENSION_X});
        editor = new Editor();
        console = new Console();
        memoryView = new FixedScrollTable(new String[]{"Address", "Value"}, 100);
        syntaxStyle = new StyleGroup();
        referenceStyle = new StyleGroup();

        Logger.setConsole(console);

        setupWindow();

        JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        controlPanel.add(memoryConsole, BorderLayout.LINE_START);

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

        setupButtons();
        setupMenu();
        setupEditor();

        add(controlPanel, BorderLayout.LINE_START);
        add(editor, BorderLayout.CENTER);


        updateHighlighting();
        updateMemoryTable();
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
        final JMenuBar menuBar = new MenuBuilder()
                .addMenu("File").setMnemonic('F')
                .addItem("New", () -> updateFile(fileManager::newFile), "control N")
                .addItem("Load", () -> updateFile(fileManager::load), "control L")
                .separator()
                .addItem("Save", () -> {
                    try {
                        if (!fileManager.isOnDisk()) {
                            fileManager.saveAs();
                        } else {
                            save();
                        }
                    } catch (IllegalArgumentException ignored) { }
                }, "control S")
                .addItem("Save as", fileManager::saveAs, "control shift S")
                .addItem("Quit", this::quit)
                .addMenu("Edit").setMnemonic('E')
                .addItem("Undo", editor::undo, "control Z")
                .addItem("Redo", editor::redo, "control shift Z")
                .addMenu("View").setMnemonic('V')
                .addItem("Zoom In", () -> editor.setFontSize(editor.getFontSize() + 1), "control PLUS")
                .addItem("Zoom Out", () -> editor.setFontSize(editor.getFontSize() - 1), "control MINUS")
                .separator()
                .addItem(binaryView, this::updateMemoryTable)
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
        controlPanel.add(buttonPanel, BorderLayout.PAGE_START);
        controlPanel.add(buttonPanel, BorderLayout.PAGE_START);
    }

    /**
     * Setup the editor
     */
    private void setupEditor() {
        StyleGroup defaultStyle = new StyleGroup();
        Style style = new StyleContext().addStyle("default", null);
        style.addAttribute(HighlightView.JAGGED_UNDERLINE, new Color(0xd25252));
        defaultStyle.addHighlight("[^\\s]*", style);

        defaultStyle.addHighlight(Keyword.getKeywords(), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.OPEN_BRACKET), SyntaxColor.TEXT);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.CLOSED_BRACKET), SyntaxColor.TEXT);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_OPEN), SyntaxColor.SCOPE);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_CLOSED), SyntaxColor.SCOPE);
        defaultStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_BEGIN), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.INSTRUCTION_END), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.COMMA), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight("-?[0-9]+", SyntaxColor.NUMBER);
        defaultStyle.addHighlight(Punctuation.BINARY_PREFIX + "[10]*", SyntaxColor.BINARY);
        StyleGroup commentStyle = new StyleGroup();
        commentStyle.addHighlight(Punctuation.COMMENT + "[^\n" + Punctuation.COMMENT + "]*" + Punctuation.COMMENT + '?',
                SyntaxColor.COMMENT);

        editor.addStyleGroup(defaultStyle);
        editor.addStyleGroup(syntaxStyle);
        editor.addStyleGroup(referenceStyle);
        editor.addStyleGroup(commentStyle);
        editor.addAfterEditAction(() -> fileManager.setText(editor.getText()));
        editor.addAfterEditAction(() -> {
            /* no need to error while writing*/
            try {
                controller.parse(editor.getText(), getInstructionSet());
            } catch (IllegalArgumentException | IllegalStateException ignored) { }
        });
        editor.addAfterEditAction(this::updateReferenceHighlighting);
        editor.useStyle(true);
        editor.clean();
        editor.useHistory(true, HISTORY_CAPACITY);
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
        try {
            console.clear();
            loadAction.run();
            String text = fileManager.getText();
            editor.setText(text);
            setTitle(TITLE + ' ' + fileManager.getLastFile().replaceAll(" ", ""));
            Logger.log("loaded: " + FileName.shorten(fileManager.getLastFile(), MAX_FILE_DISPLAY_LENGTH));
            controller.parse(text, getInstructionSet());
            updateHighlighting();
            editor.resetHistory();
            editor.clean();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Logger.error(e.getMessage());
        }
    }

    /*
     * Get the current instruction set
     */
    private InstructionSet getInstructionSet() {
        return fileManager.getLastExtension().equals(FILE_EXTENSION_X)
                ? InstructionSet.MIMA_X
                : InstructionSet.MIMA;
    }

    /**
     * Perform one instruction of the mima.
     */
    private void step() {
        try {
            controller.step();
            if (controller.getCurrentStatement() != null) {
                Logger.log("Instruction: " + controller.getCurrentStatement().simpleName());
            }
            run.setEnabled(false);
            updateMemoryTable();
            run.setEnabled(!controller.isRunning());
            step.setEnabled(controller.isRunning());
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
        Logger.log("Running program: " + FileName.shorten(fileManager.getLastFile(), MAX_FILE_DISPLAY_LENGTH) + "...");
        try {
            controller.run();
            updateMemoryTable();
            step.setEnabled(true);
            run.setEnabled(true);
            Logger.log("(done)");
        } catch (final InterpreterException e) {
            Logger.error('\n' + e.getMessage());
            step.setEnabled(false);
            run.setEnabled(false);
        }
    }

    /**
     * Save current file
     */
    private void save() {
        try {
            Logger.log("saving " + FileName.shorten(fileManager.getLastFile(), MAX_FILE_DISPLAY_LENGTH) + "...");
            fileManager.save();
            Logger.log("(done)");
        } catch (final IllegalArgumentException | IOException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Compile mima program
     */
    private void compile() {
        controller.stop();
        Logger.log("Compiling: " + FileName.shorten(fileManager.getLastFile(), MAX_FILE_DISPLAY_LENGTH) + "...");
        try {
            controller.parse(editor.getText(), getInstructionSet());
            controller.checkCode();
            updateMemoryTable();
            run.setEnabled(true);
            step.setEnabled(true);
            Logger.log("(done)");
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
        memoryView.setContent(controller.getMemoryTable(binaryView.isSelected()));
        repaint();
    }

    /**
     * Update the style groups for syntax highlighting
     */
    private void updateHighlighting() {
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
    }

    /**
     * Update the syntax highlighting according to the current instruction set
     */
    private void updateSyntaxHighlighting() {
        syntaxStyle.setHighlight(Arrays.stream(getInstructionSet().getInstructions()).map(
                s -> "(?<=[\\s\\(,])" + s + "(?=[\\(,:;\\s])"
        ).toArray(String[]::new), SyntaxColor.INSTRUCTION);
        syntaxStyle.addHighlight("(?<=[\\s\\(,])HALT(?=[\\(,:;\\s])", SyntaxColor.WARNING);
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
            referenceStyle.setHighlight(constants, SyntaxColor.CONSTANT);
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
