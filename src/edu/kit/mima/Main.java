package edu.kit.mima;

import edu.kit.mima.core.*;
import edu.kit.mima.gui.*;
import edu.kit.mima.gui.console.Console;
import edu.kit.mima.gui.editor.*;
import edu.kit.mima.gui.loading.*;
import edu.kit.mima.gui.menu.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    private final TextLoader textLoader;
    private final OptionsLoader optionsLoader;
    private final SaveHandler saveHandler;
    private final Console console;
    private final Editor editor;
    private final MemoryView memoryView;
    private final JButton run = new JButton("RUN");
    private final JButton step = new JButton("STEP");
    private boolean unsaved;
    private String lastFile;
    private String directory;
    private String[] lines;
    private boolean mimaXFile;


    public Main() {
        optionsLoader = new OptionsLoader(MIMA_DIR);
        saveHandler = new SaveHandler(MIMA_DIR);
        editor = new Editor();
        console = new Console();
        memoryView = new MemoryView(new String[]{"Address", "Value"});
        setupWindow();

        JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        add(memoryConsole, BorderLayout.LINE_START);

        setupButtons();
        setupMenu();
        add(editor, BorderLayout.CENTER);

        LoadManager loadManager = new DefaultLoadManager() {
            @Override
            public void onLoad(String path) { log("Loading: " + path + "..."); }

            @Override
            public void afterRequest(File chosenFile) {
                mimaXFile = (chosenFile.getAbsolutePath().endsWith(FILE_EXTENSION_X));
                lastFile = chosenFile.getAbsolutePath();
                directory = chosenFile.getParentFile().getAbsolutePath();
            }

            @Override
            public void afterLoad() { log("done"); }

            @Override
            public void onSave(String path) { log("Saving: " + path + "..."); }

            @Override
            public void afterSave() { log("done"); }

            @Override
            public void onFail(String errorMessage) { error(errorMessage); }
        };
        textLoader = new TextLoader(this, loadManager);

        mima = new Mima();
        //Load Options
        try {
            String[] options = optionsLoader.loadOptions();
            this.lastFile = options[0];
            this.directory = new File(lastFile).getParentFile().getAbsolutePath();
        } catch (IOException e) {
            this.directory = System.getProperty("user.home");
        }
        //Load Last File
        try {
            String text = saveHandler.loadFile(lastFile);
            editor.setText(text);
            lines = text.split("\n");
            mimaXFile = lastFile.endsWith(FILE_EXTENSION_X);
        } catch (IOException e) {
            firstStart();
        }
        updateSyntaxHighlighting();
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
                    if (unsaved) savePopUp();
                    optionsLoader.saveOptions(lastFile);
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
        newFile.addActionListener(e -> newFile());
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        file.add(newFile);
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(e -> load(textLoader.requestLoad(directory,
                                                                new String[]{FILE_EXTENSION, FILE_EXTENSION_X},
                                                                () -> { })));
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        file.add(load);

        file.addSeparator();

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        save.addActionListener(e -> {
            if (unsaved) saveAs();
            else save();
        });
        file.add(save);
        JMenuItem saveAs = new JMenuItem("Save as");
        saveAs.addActionListener(e -> saveAs());
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

    private void firstStart() {
        try {
            int response = JOptionPane
                    .showOptionDialog(this, "Create/Load File", TITLE, JOptionPane.DEFAULT_OPTION,
                                      JOptionPane.PLAIN_MESSAGE,
                                      null, new String[]{"Load", "New"}, "New");
            if (response == 0) { //Load
                load(textLoader.requestLoad(directory, new String[]{FILE_EXTENSION, FILE_EXTENSION_X},
                                            () -> System.exit(0)));
            } else if (response == 1) { //New
                newFile();
            } else { //Abort
                System.exit(0);
            }
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void step() {
        try {
            log("Step!");
            mima.step();
            run.setEnabled(false);
            log("Instruction: " + lines[mima.getCurrentLineIndex() - 1]);
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
        log("Running program: " + lastFile + "...");
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


    private void newFile() {
        int response = JOptionPane.showOptionDialog(this, "Choose file type", "File type",
                                                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                                    null, new String[]{"Mima", "MimaX"}, "Mima");
        if (response == JOptionPane.CLOSED_OPTION) return;
        mimaXFile = response == 1;
        updateSyntaxHighlighting();
        lines = new String[]{"#Put Code here"};
        editor.setText("#Put Code here");
        unsaved = true;
    }

    private void load(String text) {
        String corrected = text.replaceAll("\r?\n", "\n");
        editor.setText(corrected);
        lines = corrected.split("\n");
        updateSyntaxHighlighting();
        editor.stylize();
        unsaved = false;
    }

    private void reloadMima() {
        try {
            mima.loadProgram(lines.clone(), mimaXFile);
            run.setEnabled(false);
            step.setEnabled(false);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void save() {
        try {
            log("saving...");
            saveHandler.saveFile(editor.getText(), lastFile);
            log("done");
        } catch (IOException e) {
            error("failed to save: " + e.getMessage());
        }
    }

    private void saveAs() {
        String extension = mimaXFile ? FILE_EXTENSION_X : FILE_EXTENSION;
        try {
            textLoader.requestSave(editor.getText(), directory, extension,
                                   () -> { throw new IllegalArgumentException(); });
        } catch (IllegalArgumentException ignored) { }
        unsaved = false;
    }

    private void savePopUp() {
        int response = JOptionPane.showOptionDialog(Main.this, "Do you want to save?", "Unsaved File",
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.WARNING_MESSAGE,
                                                    null, new String[]{"Save", "Don't save"}, "Save");
        if (response == JOptionPane.OK_OPTION) {
            saveAs();
        } else if (response == JOptionPane.CLOSED_OPTION) {
            throw new IllegalArgumentException("Window not closed");
        }
    }

    private void compile() {
        log("Compiling: " + lastFile + "...");
        try {
            lines = editor.getText().split("\n");
            reloadMima();
            run.setEnabled(true);
            step.setEnabled(true);
            updateMemoryTable();
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
        editor.setHighlight(Mima.getInstructionSet(), instructionsColor);
        editor.addHighlight(Interpreter.getKeywords(), new Color[]{
                keywordColor, //$define
                keywordColor, // :
                keywordColor, // (
                keywordColor, // )
                new Color(37, 143, 148), //Numbers
                new Color(136, 64, 170), //0b,
                new Color(63, 135, 54), //Comments
        });
        if (mimaXFile) {
            editor.addHighlight(Mima.getMimaXInstructionSet(), instructionsColor);
        }
    }

    private void log(String message) {
        console.log(message);
        repaint();
    }

    private void error(String message) {
        console.error(message);
        repaint();
    }

}
