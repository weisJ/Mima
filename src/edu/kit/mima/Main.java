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
    private static final String FILE_EXTENSION = ".mima";
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
    private String savedPath;
    private String[] lines;


    public Main() {
        optionsLoader = new OptionsLoader(MIMA_DIR);
        saveHandler = new SaveHandler(MIMA_DIR);
        editor = new Editor();
        setSyntaxHighlighting();
        console = new Console();
        memoryView = new MemoryView(new String[]{"Address", "Value"});

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    saveHandler.deleteTmp();
                    optionsLoader.saveOptions(savedPath);
                    saveHandler.saveTmp(editor.getText());
                } catch (IOException ignored) {
                } finally {
                    e.getWindow().dispose();
                }
            }
        });
        setResizable(true);
        setSize((int) FULLSCREEN.getWidth() / 2, (int) FULLSCREEN.getHeight() / 2);
        setTitle(TITLE);
        setLayout(new BorderLayout());
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("mima.png")));

        JPanel memoryConsole = new JPanel(new GridLayout(2, 1));
        memoryConsole.add(memoryView);
        memoryConsole.add(console);
        add(memoryConsole, BorderLayout.LINE_START);

        setupButtons();
        Menu menu = new Menu();
        JMenu file = new JMenu("File");
        JMenuItem newFile = new JMenuItem("New");
        newFile.addActionListener(e -> editor.setText(""));
        file.add(newFile);
        menu.add(file);
        setJMenuBar(menu);
        add(editor, BorderLayout.CENTER);

        LoadManager loadManager = new DefaultLoadManager() {
            @Override
            public void onLoad(String path) { log("Loading: " + path + "..."); }

            @Override
            public void afterRequest(File chosenFile) { savedPath = chosenFile.getParentFile().getAbsolutePath(); }

            @Override
            public void afterLoad() { log("done"); }

            @Override
            public void onSave(String path) { log("Saving: " + path + "..."); }

            @Override
            public void afterSave() { log("done"); }

            @Override
            public void onFail(String errorMessage) { error(errorMessage); }
        };
        textLoader = new TextLoader(this, FILE_EXTENSION, loadManager);

        mima = new Mima();
        //Load Options
        try {
            savedPath = optionsLoader.loadOptions();
        } catch (IOException e) {
            savedPath = System.getProperty("user.home");
        }
        //Load Tmp
        try {
            String text = saveHandler.loadTmp();
            editor.setText(text);
            lines = text.split("\n");
        } catch (IOException e) {
            firstStart();
        }
        editor.setStylize(true);
        editor.stylize();
    }


    public static void main(String[] args) {
        Main frame = new Main();
        frame.setVisible(true);
        frame.repaint();
    }

    private void firstStart() {
        try {
            int response = JOptionPane
                    .showOptionDialog(this, "Create/Load File", TITLE, JOptionPane.DEFAULT_OPTION,
                                      JOptionPane.PLAIN_MESSAGE,
                                      null, new String[]{"Load", "New"}, "New");
            if (response == 0) { //Load
                load(textLoader.requestLoad(savedPath, () -> System.exit(0)));
            } else if (response == 1) { //New
                lines = new String[]{"#Put Code here"};
                editor.setText("#Put Code here");
            } else { //Abort
                System.exit(0);
            }
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void run() {
        step.setEnabled(false);
        run.setEnabled(false);
        log("Running program: " + savedPath + "...");
        try {
            mima.run();
            update();
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
            mima.reset();
        } finally {
            step.setEnabled(true);
            run.setEnabled(true);
        }
        log("done");
    }

    private void step() {
        try {
            log("Step!");
            mima.step();
            run.setEnabled(false);
            log("Instruction: " + lines[mima.getCurrentLineIndex() - 1]);
            update();
            step.setEnabled(mima.isRunning());
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void load(String text) {
        String corrected = text.replaceAll("\r?\n", "\n");
        editor.setText(corrected);
        lines = corrected.split("\n");
    }

    private void update() {
        memoryView.setContent(mima.memoryTable());
        editor.stylize();
        repaint();
    }

    private void setupButtons() {
        JButton save = new JButton("SAVE");
        save.addActionListener(e -> textLoader.requestSave(editor.getText(), savedPath, () -> { }));
        run.addActionListener(e -> run());
        step.addActionListener(e -> step());
        JButton load = new JButton("LOAD");
        load.addActionListener(e -> load(textLoader.requestLoad(savedPath, () -> { })));
        JButton compile = new JButton("COMPILE");
        compile.addActionListener(e -> compile());
        JButton reset = new JButton("RESET");
        reset.addActionListener(e -> {
            mima.reset();
            run.setEnabled(true);
            step.setEnabled(true);
            update();
        });

        run.setEnabled(false);
        step.setEnabled(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 5));
        panel.add(save);
        panel.add(load);
        panel.add(compile);
        panel.add(reset);
        panel.add(step);
        panel.add(run);
        add(panel, BorderLayout.PAGE_START);
    }

    private void setSyntaxHighlighting() {
        editor.setHighlight(Mima.getInstructionSet(), new Color(27, 115, 207));
        editor.setHighlight(Interpreter.getKeywords(), new Color[]{
                new Color(168, 120, 43), //$define
                new Color(168, 120, 43), // :
                new Color(37, 143, 148), //Numbers
                new Color(136, 64, 170), //0b,
                new Color(63, 135, 54), //Comments
        });
    }

    private void compile() {
        log("Compiling: " + savedPath + "...");
        try {
            lines = editor.getText().split("\n");
            reloadMima();
            run.setEnabled(true);
            step.setEnabled(true);
            update();
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
        log("done");
    }

    private void log(String message) {
        console.log(message);
        repaint();
    }

    private void error(String message) {
        console.error(message);
        repaint();
    }

    private void reloadMima() {
        try {
            mima.loadProgram(lines.clone());
            run.setEnabled(false);
            step.setEnabled(false);
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }
}
