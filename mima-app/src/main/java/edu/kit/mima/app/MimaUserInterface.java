package edu.kit.mima.app;

import edu.kit.mima.App;
import edu.kit.mima.core.Debugger;
import edu.kit.mima.core.MimaCompiler;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.gui.EditorHotKeys;
import edu.kit.mima.gui.components.FixedScrollTable;
import edu.kit.mima.gui.components.ZeroWidthSplitPane;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.folderdisplay.FileDisplay;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane;
import edu.kit.mima.gui.menu.Help;
import edu.kit.mima.gui.menu.settings.Settings;
import edu.kit.mima.gui.view.MemoryTableView;
import edu.kit.mima.loading.FileManager;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.util.BindingUtil;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
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
public final class MimaUserInterface extends JFrame {

    private static final Dimension FULLSCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String TITLE = "Mima-IDE";

    private final MimaEditorManager editorManager = new MimaEditorManager(this);
    private final MimaRunner mimaRunner = new MimaRunner();
    private final Debugger debugger = mimaRunner.debugger();
    private final FileActions fileActions = new FileActions(this, editorManager);
    private final RunActions runActions = new RunActions(this,
                                                         new MimaCompiler(),
                                                         mimaRunner,
                                                         debugger);

    private final FileDisplay fileDisplay;
    private final EditorTabbedPane tabbedEditor;
    private final Console console;
    private final MemoryTableView memoryView;
    private final FixedScrollTable memoryTable;
    private final JPanel controlPanel = new JPanel(new BorderLayout());

    /**
     * Create a new Mima UI window.
     *
     * @param filePath path of file to open
     */
    public MimaUserInterface(@Nullable final String filePath) {
        fileDisplay = new MimaFileDisplay(fileActions).getDisplay();
        tabbedEditor = editorManager.getTabbedEditor();
        console = new Console();
        memoryTable = new FixedScrollTable(new String[]{"Address", "Value"}, 100);
        memoryView = new MemoryTableView(mimaRunner, memoryTable);

        App.logger.setConsole(console);
        createBinding();
        startSession(filePath);
        setupWindow();
        setupComponents();
        memoryView.updateView();
    }

    private void createBinding() {
        BindingUtil.bind(debugger, () -> {
            editorManager.currentEditor().markLine(
                    Optional.ofNullable(mimaRunner.getCurrentStatement()).map(Token::getOffset)
                            .orElse(-1));
            memoryView.updateView();
        }, Debugger.PAUSE_PROPERTY);
        BindingUtil.bind(mimaRunner, memoryView::updateView,
                         MimaRunner.RUNNING_PROPERTY);
        BindingUtil.bind(debugger, () -> currentEditor().markLine(-1),
                         Debugger.RUNNING_PROPERTY);
        fileDisplay.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                fileDisplay.setFile(new File(editorManager.currentFileManager().getLastFile()));
            }
        });
        tabbedEditor.addChangeListener(e -> {
            final Editor editor = (Editor) tabbedEditor.getSelectedComponent();
            if (editor == null) {
                return;
            }
            var file = new File(Optional.ofNullable(editorManager.managerForEditor(editor))
                                        .map(FileManager::getLastFile)
                                        .orElse(System.getProperty("SystemDrive")));

            fileDisplay.setFile(file);
            EditorHotKeys.setEditor(editor);
        });
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

    private void startSession(@Nullable final String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            final String filesString = Preferences.getInstance().readString(PropertyKey.LAST_FILE);
            if (filesString.length() < 2) {
                return;
            }
            final String[] files = filesString.split("/");
            for (final String file : files) {
                fileActions.openFile(file);
            }
        } else {
            fileActions.openFile(filePath);
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
        controlPanel.add(new MimaButtonArea(this, tabbedEditor, runActions).getPane(),
                         BorderLayout.EAST);
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
        setJMenuBar(new MimaMenuBar(this, fileActions).getMenuBar());
    }

    /**
     * Quit the program.
     */
    public void quit() {
        try {
            editorManager.close();
            Preferences.getInstance().saveOptions();
            dispose();
            Settings.close();
            Help.close();
            mimaRunner.stop();
            System.exit(0);
        } catch (final IllegalArgumentException ignored) {
        } catch (final IOException e) {
            App.logger.error(e.getMessage());
        }
    }

    /**
     * Update window title to current file name.
     */
    public void fileChanged() {
        final String file = editorManager.currentFileManager().getLastFile();
        setTitle(TITLE + ' ' + FileName.shorten(file));
        final File parent = new File(file).getParentFile();
        final var pref = Preferences.getInstance();
        final String workDir = parent != null
                ? parent.getAbsolutePath()
                : pref.readString(PropertyKey.DIRECTORY_MIMA);
        pref.saveString(PropertyKey.DIRECTORY_WORKING, workDir);
        pref.saveString(PropertyKey.LAST_FILE, file);
    }
}
