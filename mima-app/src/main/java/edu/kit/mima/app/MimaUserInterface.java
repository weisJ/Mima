package edu.kit.mima.app;

import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.App;
import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SwingSubscriber;
import edu.kit.mima.api.util.FileName;
import edu.kit.mima.core.Debugger;
import edu.kit.mima.core.MimaCompiler;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.gui.EditorHotKeys;
import edu.kit.mima.gui.components.ImmutableScrollTable;
import com.weis.darklaf.components.alignment.Alignment;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.gui.components.console.SystemConsole;
import edu.kit.mima.gui.components.filetree.FileTree;
import edu.kit.mima.gui.components.folderdisplay.FilePathDisplay;
import edu.kit.mima.gui.components.listeners.MouseClickListener;
import edu.kit.mima.gui.components.tabbedpane.EditorTabbedPane;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import edu.kit.mima.gui.components.tabframe.popuptab.DefaultPopupComponent;
import edu.kit.mima.gui.components.tabframe.popuptab.TerminalPopupComponent;
import edu.kit.mima.gui.components.text.editor.Editor;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.settings.Settings;
import edu.kit.mima.gui.persist.PersistenceManager;
import edu.kit.mima.gui.view.AssemblerView;
import edu.kit.mima.gui.view.MemoryTableView;
import edu.kit.mima.loading.FileManager;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Mima Editor Frame.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaUserInterface extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-IDE";

    private final MimaEditorManager editorManager = new MimaEditorManager(this);
    private final MimaRunner mimaRunner = new MimaRunner();
    private final Debugger debugger = mimaRunner.debugger();
    private final FileActions fileActions = new FileActions(this, editorManager);
    private final RunActions runActions =
            new RunActions(this, new MimaCompiler(), mimaRunner, debugger);

    @NotNull
    private final FilePathDisplay filePathDisplay;
    @NotNull
    private final EditorTabbedPane tabbedEditor;
    @NotNull
    private final Console console;
    @NotNull
    private final MemoryTableView memoryView;
    private final AssemblerView assemblerView;
    @NotNull
    private final ImmutableScrollTable memoryTable;
    private JPanel buttonArea;
    private JPanel controlPanel;

    /**
     * Create a new Mima UI window.
     *
     * @param filePath path of file to open
     */
    public MimaUserInterface(@Nullable final String filePath) {
        filePathDisplay = new MimaFileDisplay(fileActions).getDisplay();
        tabbedEditor = editorManager.getTabbedEditor();
        console = new Console();
        memoryTable = new ImmutableScrollTable(new String[]{"Address", "Value"},
                                               100, new Insets(0, 5, 0, 0));
        memoryView = new MemoryTableView(mimaRunner, memoryTable);
        assemblerView = new AssemblerView();

        App.logger.setConsole(console);
        createSubscriptions();
        setupWindow();
        setupComponents();
        startSession(filePath);
        memoryView.updateView();
    }

    private void createSubscriptions() {
        final var subscriptionManager = SubscriptionManager.getCurrentManager();

        subscriptionManager.subscribe(new SwingSubscriber<Boolean>((identifier, value) -> {
            if (value) {
                int index = Optional.ofNullable(mimaRunner.getCurrentStatement())
                                    .map(Token::getOffset)
                                    .orElse(-1);
                editorManager.currentEditor().markLine(index);
                memoryView.updateView();
            }
        }), Debugger.PAUSE_PROPERTY);

        subscriptionManager.subscribe(new SwingSubscriber<Boolean>((identifier, value) -> {
            if (!value) {
                memoryView.updateView();
            }
        }), MimaRunner.RUNNING_PROPERTY);

        subscriptionManager.subscribe(new SwingSubscriber<Boolean>((identifier, value) -> {
            if (!value) {
                currentEditor().markLine(-1);
                memoryView.updateView();
            }
            filePathDisplay.setMaximumSize(new Dimension(buttonArea.getX() - filePathDisplay.getX(),
                                                         controlPanel.getMinimumSize().height));
        }), Debugger.RUNNING_PROPERTY);
        subscriptionManager.subscribe(new SwingSubscriber<>((identifier, value) -> {
            var editor = editorManager.currentEditor();
            var manager = editorManager.managerForEditor(editor);
            if (editor != null && manager != null && manager.unsaved()) {
                assemblerView.setProgram(editor.getText());
            }
        }), MimaRunner.RUNNING_PROPERTY, Debugger.RUNNING_PROPERTY);
        tabbedEditor.addChangeListener(
                e -> Optional.ofNullable((Editor) tabbedEditor.getSelectedComponent())
                             .ifPresent(editor -> {
                                 var file = new File(Optional.ofNullable(editorManager.managerForEditor(editor))
                                                             .map(FileManager::getLastFile)
                                                             .orElse(System.getProperty("SystemDrive")));
                                 filePathDisplay.setFile(file);
                                 assemblerView.setProgram(editor.getText());
                                 EditorHotKeys.setEditor(editor);
                             })
        );
    }

    /**
     * Get the current editor.
     *
     * @return the current editor.
     */
    public Editor currentEditor() {
        return editorManager.currentEditor();
    }

    /**
     * Get the current file manager.
     *
     * @return the current file manager.
     */
    public FileManager currentFileManager() {
        return editorManager.currentFileManager();
    }

    /**
     * Start session.
     *
     * @param filePath path to open or null if session should be restored.
     */
    private void startSession(@Nullable final String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            final String filesString = Preferences.getInstance().readString(PropertyKey.LAST_FILE);
            final String[] files = filesString.isEmpty() ? new String[0] : filesString.split("/");
            for (final String file : files) {
                openFile(file);
            }
        } else {
            openFile(filePath);
        }
    }

    public void openFile(final String path) {
        if (path != null && !path.isEmpty()) {
            fileActions.openFile(path);
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
        var image = Toolkit.getDefaultToolkit()
                            .getImage(getClass().getClassLoader().getResource("images/mima.png"));
        setIconImage(image);
    }

    private void setupComponents() {
        final JPanel contentPane = new JPanel(new BorderLayout());
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(filePathDisplay, BorderLayout.WEST);
        buttonArea = new MimaButtonArea(this, runActions).getPane();

        controlPanel.add(buttonArea, BorderLayout.EAST);
        controlPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        controlPanel.setComponentZOrder(filePathDisplay, 1);
        controlPanel.setComponentZOrder(buttonArea, 0);

        var fileTree =  new FileTree(new File(Preferences.getInstance().readString(PropertyKey.DIRECTORY_WORKING)));
        fileTree.getTree().addMouseListener((MouseClickListener) e -> {
            if (e.getClickCount() == 2) {
                var tree = fileTree.getTree();
                TreePath pathForLocation = tree.getClosestPathForLocation(e.getX(), e.getY());
                if (pathForLocation != null) {
                    if (!tree.isPathSelected(pathForLocation)) {
                        tree.setSelectionPath(pathForLocation);
                    }
                    var file = fileTree.getSelectedFile();
                    openFile(file.getAbsolutePath());
                }
            }
        });

        contentPane.add(controlPanel, BorderLayout.NORTH);
        var tabFrame = new TabFrame();
        tabFrame.setPersistable(true, "mainTabFrame");
        tabFrame.setContentPane(tabbedEditor);
        tabFrame.addTab(new DefaultPopupComponent("Memory", Icons.MEMORY, memoryTable),
                        "Memory",
                        Icons.MEMORY,
                        Alignment.EAST);
        tabFrame.addTab(new DefaultPopupComponent("Console", Icons.CONSOLE, console),
                        "Console",
                        Icons.CONSOLE,
                        Alignment.SOUTH_WEST);
        tabFrame.addTab(new TerminalPopupComponent("Terminal"),
                        "Terminal",
                        Icons.TERMINAL, Alignment.SOUTH_WEST);
        tabFrame.addTab(new DefaultPopupComponent("Developer Console", Icons.BUILD_GREY, new SystemConsole()),
                        "Developer",
                        Icons.BUILD_GREY, Alignment.SOUTH);
        tabFrame.addTab(new DefaultPopupComponent("Files", Icons.PROJECT, fileTree),
                        "Files",
                        Icons.FOLDER, Alignment.NORTH_WEST);
        tabFrame.addTab(new DefaultPopupComponent("Assembly", Icons.ASSEMBLY_FILE, assemblerView),
                        "Assembly",
                        Icons.ASSEMBLY_FILE, Alignment.EAST);
        contentPane.add(tabFrame, BorderLayout.CENTER);
        setContentPane(contentPane);
        setJMenuBar(new MimaMenuBar(this, fileActions).getMenuBar());
        pack();
    }

    /**
     * Quit the program.
     */
    public void quit() {
        try {
            editorManager.close();
            Preferences.getInstance().saveOptions();
            PersistenceManager.getInstance().saveStates();
            dispose();
            Settings.close();
            Help.getInstance().close();
            mimaRunner.stop();
            System.exit(0);
        } catch (@NotNull final IllegalArgumentException ignored) {
        } catch (@NotNull final IOException e) {
            App.logger.error(e.getMessage());
        }
    }

    /**
     * Update window title to current file name.
     */
    public void fileChanged() {
        Optional.ofNullable(editorManager.currentFileManager()).map(FileManager::getLastFile).stream()
                .peek(f -> filePathDisplay.setFile(new File(f)))
                .peek(f -> setTitle(TITLE + ' ' + FileName.shorten(f)))
                .findFirst()
                .ifPresent(f -> {
                    final File parent = new File(f).getParentFile();
                    final var pref = Preferences.getInstance();
                    final String workDir = parent != null
                                           ? parent.getAbsolutePath()
                                           : pref.readString(PropertyKey.DIRECTORY_MIMA);
                    pref.saveString(PropertyKey.DIRECTORY_WORKING, workDir);
                });
    }
}
