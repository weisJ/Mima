package edu.kit.mima.app;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.gui.EditorHotKeys;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.editor.highlighter.MimaHighlighter;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.loading.FileManager;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Manager for mima files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaEditorManager implements AutoCloseable {

    @NotNull private final Map<Editor, FileManager> fileManagers;
    @NotNull private final EditorTabbedPane tabbedEditor;
    private final MimaUserInterface parent;
    private Tuple<Editor, FileManager> cashed;

    /**
     * Cretae new Editor Manager for Mima App.
     *
     * @param parent parent App.
     */
    public MimaEditorManager(MimaUserInterface parent) {
        fileManagers = new HashMap<>();
        tabbedEditor = createEditorPane();
        this.parent = parent;

    }

    /**
     * Get the tabbed editor Pane.
     *
     * @return the tabbed editor pane.
     */
    @NotNull
    public EditorTabbedPane getTabbedEditor() {
        return tabbedEditor;
    }

    private EditorTabbedPane createEditorPane() {
        var pane = new EditorTabbedPane();
        pane.addTabClosedEventHandler(c -> {
            final Editor editor = (Editor) c;
            try {
                closeEditor(editor);
            } catch (final IOException e) {
                throw new IllegalArgumentException("didn't save", e);
            }
        });
        return pane;
    }

    /*
     * Close an editor.
     */
    private void closeEditor(@NotNull final Editor editor) throws IOException {
        final var fm = fileManagers.get(editor);
        if (fm.unsaved()) {
            fm.savePopUp(() -> {
                throw new IllegalArgumentException("aborted");
            });
        }
        fm.close();
        editor.close();
    }

    /**
     * Create an editor with corresponding file manager.
     *
     * @return Pair of editor and file manager.
     */
    @NotNull
    public Tuple<Editor, FileManager> createEditor() {
        final var fm = new FileManager(parent, MimaConstants.EXTENSIONS);
        final Editor editor = new Editor();
        var highlighter = new MimaHighlighter();
        fm.addFileEventHandler(highlighter);
        editor.setRepaint(false);
        editor.setHighlighter(highlighter);
        editor.addEditEventHandler(() -> fm
                .setText(editor.getText().replaceAll(String.format("%n"), "\n")));
        editor.useHistory(true,
                          Preferences.getInstance().readInteger(PropertyKey.EDITOR_HISTORY_SIZE));
        editor.showCharacterLimit(80); //Todo Preference
        editor.setText(fm.getText());
        editor.setRepaint(false);
        setupHotKeys(editor);
        cashed = new ValueTuple<>(editor, fm);
        return cashed;
    }

    /**
     * Open the editor.
     *
     * @param editor      editor to open.
     * @param fileManager corresponding file manager.
     */
    public void openEditor(Editor editor, FileManager fileManager) {
        String lastFile = fileManager.getLastFile();
        for (final var entry : fileManagers.entrySet()) {
            if (entry.getValue() != fileManager
                    && entry.getValue().getLastFile().equals(lastFile)) {
                tabbedEditor.setSelectedComponent(entry.getKey());
                return;
            }
        }
        editor.setText(fileManager.getText());
        lastFile = lastFile.substring(Math.max(Math.min(lastFile.lastIndexOf('\\') + 1,
                                                        lastFile.length() - 1), 0));
        tabbedEditor.addTab(lastFile, Icons.forFile(lastFile), editor);
        fileManagers.put(editor, fileManager);

        editor.setRepaint(true);
        editor.resetHistory();
        editor.update();
    }

    /**
     * Setup HotKey functionality.
     */
    private void setupHotKeys(@NotNull final Editor editor) {
        EditorHotKeys.setEditor(editor);
        for (final EditorHotKeys key : EditorHotKeys.values()) {
            editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key.getAccelerator()), key.toString());
            editor.getActionMap().put(key.toString(), key);
        }
    }

    /**
     * Get the current Editor.
     *
     * @return current selected editor.
     */
    public Editor currentEditor() {
        if (fileManagers.size() <= 1) {
            return fileManagers.keySet().stream().findFirst().orElse(null);
        } else {
            return (Editor) tabbedEditor.getComponentAt(tabbedEditor.getSelectedIndex());
        }
    }

    /**
     * Get the current file Manager.
     *
     * @return the current file manager.
     */
    public FileManager currentFileManager() {
        if (fileManagers.size() <= 1) {
            return fileManagers.values().stream().findFirst().orElse(null);
        } else {
            return fileManagers.get(currentEditor());
        }
    }

    /**
     * Get file manager for editor.
     *
     * @param editor editor
     * @return the file manger for the editor.
     */
    @Nullable
    public FileManager managerForEditor(final Editor editor) {
        if (fileManagers.containsKey(editor)) {
            return fileManagers.get(editor);
        } else if (cashed.getFirst().equals(editor)) {
            return cashed.getSecond();
        } else {
            return null;
        }
    }


    @Override
    public void close() throws IOException {
        final StringBuilder openFiles = new StringBuilder();
        for (final var fm : fileManagers.values()) {
            if (fm.unsaved()) {
                fm.savePopUp(() -> {
                    throw new IllegalArgumentException("aborted");
                });
            }
            openFiles.append(fm.getLastFile()).append("/");
            fm.close();
        }
        final var pref = Preferences.getInstance();
        pref.saveString(PropertyKey.LAST_FILE, openFiles.toString());
    }
}
