package edu.kit.mima;

import edu.kit.mima.core.*;
import edu.kit.mima.gui.*;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.editor.*;
import edu.kit.mima.gui.logging.*;
import edu.kit.mima.gui.menu.Menu;

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
public final class Main extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-Simulator";
    private static final String FILE_EXTENSION = "mima";
    private static final String FILE_EXTENSION_X = "mimax";
    private static final String MIMA_DIR = System.getProperty("user.home") + "/.mima";

    private final Mima mima;
    private final FileManager fileManager;
    private final MemoryView memoryView;

    private final Editor editor;
    private final StyleGroup syntaxStyle;
    private final StyleGroup referenceStyle;

    private final JButton run = new JButton("RUN");
    private final JButton step = new JButton("STEP");

    public Main() {
        fileManager = new FileManager(this, MIMA_DIR, new String[]{FILE_EXTENSION, FILE_EXTENSION_X});
        editor = new Editor();
        Console console = new Console();
        Logger.setConsole(console);
        memoryView = new MemoryView(new String[]{"Address", "Value"});

        setupWindow();
        JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        add(memoryConsole, BorderLayout.LINE_START);
        setupButtons();
        setupMenu();
        add(editor, BorderLayout.CENTER);

        mima = new Mima();
        fileManager.loadOptions();
        fileManager.loadLastFile();
        editor.setText(fileManager.getText());
        reloadMima();

        syntaxStyle = new StyleGroup();
        referenceStyle = new StyleGroup();
        editor.addStyleGroup(syntaxStyle);
        editor.addStyleGroup(referenceStyle);
        editor.doReplaceTabs(true);
        editor.useHistory(true, 20);
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
        editor.addAfterUpdateAction(e -> fileManager.setText(editor.getText()));
        editor.addAfterUpdateAction(e -> updateReferenceHighlighting());

        editor.setStylize(true);
        editor.stylize();
    }

    public static void main(String[] args) {
        Main frame = new Main();
        frame.setVisible(true);
        frame.repaint();
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (!fileManager.isSaved()) fileManager.savePopUp(editor.getText());
                    fileManager.saveOptions();
                    e.getWindow().dispose();
                } catch (IOException | IllegalArgumentException ignored) { }
            }
        });
        setResizable(true);
        setSize((int) FULLSCREEN.getWidth() / 2, (int) FULLSCREEN.getHeight() / 2);
        setTitle(TITLE);
        setLayout(new BorderLayout());
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("mima.png")));
    }

    private void setupMenu() {
        Menu menu = new Menu();
        JMenu file = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        newFile.addActionListener(e -> {
            fileManager.newFile();
            editor.setText(fileManager.getText());
            editor.resetHistory();
        });
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        file.add(newFile);
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(e -> {
            fileManager.load();
            editor.setText(fileManager.getText());
            editor.resetHistory();
        });
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        file.add(load);

        file.addSeparator();

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        save.addActionListener(e -> {
            if (!fileManager.isOnDisk()) fileManager.saveAs(editor.getText());
            else save();
        });
        file.add(save);
        JMenuItem saveAs = new JMenuItem("Save as");
        saveAs.addActionListener(e -> fileManager.saveAs(editor.getText()));
        saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                     InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        file.add(saveAs);
        menu.add(file);
        setJMenuBar(menu);
    }

    private void setupButtons() {
        shortcutButton(run, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), this::run);
        shortcutButton(step,
                       KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                       this::step);
        JButton compile = new JButton("COMPILE");
        shortcutButton(compile, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), this::compile);
        JButton reset = new JButton("RESET");
        shortcutButton(reset,
                       KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                       this::compile);

        run.setEnabled(false);
        step.setEnabled(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 3));
        panel.add(compile);
        panel.add(reset);
        panel.add(step);
        panel.add(run);
        add(panel, BorderLayout.PAGE_START);
    }

    private void shortcutButton(JButton button, KeyStroke keyStroke, Runnable action) {
        Action buttonAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action performed");
                action.run();
            }
        };
        String name = button.getName();
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        this.getRootPane().getActionMap().put(name, buttonAction);
        button.addActionListener(e -> action.run());
    }

    private void step() {
        try {
            log("Step!");
            mima.step();
            run.setEnabled(false);
            log("Instruction: " + fileManager.lines()[mima.getCurrentLineIndex() - 1]);
            updateMemoryTable();
            step.setEnabled(mima.isRunning());
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            step.setEnabled(false);
            run.setEnabled(false);
        }
    }

    private void run() {
        step.setEnabled(false);
        run.setEnabled(false);
        log("Running program: " + fileManager.getLastFile() + "...");
        try {
            mima.run();
            updateMemoryTable();
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            reset();
        }
        step.setEnabled(true);
        run.setEnabled(true);
        log("done");
    }

    private void reset() {
        mima.reset();
        run.setEnabled(true);
        step.setEnabled(true);
        updateMemoryTable();
    }

    private void reloadMima() {
        try {
            mima.loadProgram(fileManager.lines().clone(), fileManager.getLastExtension().equals(FILE_EXTENSION_X));
            run.setEnabled(false);
            step.setEnabled(false);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void save() {
        try {
            log("saving...");
            fileManager.save();
            log("done");
        } catch (IOException e) {
            error("failed to save: " + e.getMessage());
        }
    }

    private void compile() {
        log("Compiling: " + fileManager.getLastFile() + "...");
        try {
            reloadMima();
            updateMemoryTable();
            run.setEnabled(true);
            step.setEnabled(true);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            run.setEnabled(false);
            step.setEnabled(false);
        }
        log("done");
    }

    private void updateMemoryTable() {
        memoryView.setContent(mima.memoryTable());
        repaint();
    }

    private void updateSyntaxHighlighting() {
        Color instructionsColor = new Color(27, 115, 207);
        Color keywordColor = new Color(168, 120, 43);

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

    private void updateReferenceHighlighting() {
        if (mima == null) return;
        try {
            List<Set<String>> references = mima.getReferences(fileManager.lines().clone());

            referenceStyle.setHighlight(Mima.getInstructionSet(), new Color(27, 115, 207));
            String[] constants = references.get(0)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(constants, new Color(255, 96, 179));
            String[] instructionReferences = references.get(2)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(instructionReferences, new Color(63, 135, 54));
            String[] memoryReferences = references.get(1)
                    .stream().map(s -> "(?<![^ \n])" + s + "(?![^ \n])").toArray(String[]::new);
            referenceStyle.addHighlight(memoryReferences, new Color(136, 37, 170));
        } catch (IllegalArgumentException ignored) { }
    }

}
