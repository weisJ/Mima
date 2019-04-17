package edu.kit.mima.app;

import edu.kit.mima.App;
import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.loading.FileManager;
import edu.kit.mima.logger.LoadingIndicator;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Actions for file management.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class FileActions {

    private final MimaUserInterface mimaUI;
    private final MimaEditorManager editorManager;

    /**
     * File loading/savings actions for mima app.
     *
     * @param mimaUI        parent app.
     * @param editorManager editor manager of app.
     */
    public FileActions(final MimaUserInterface mimaUI, final MimaEditorManager editorManager) {
        this.mimaUI = mimaUI;
        this.editorManager = editorManager;
    }

    /**
     * Open file from path.
     *
     * @param path file path.
     */
    public void openFile(@NotNull final String path) {
        openFile(fm -> {
            try {
                fm.load(path);
            } catch (@NotNull final IOException e) {
                App.logger.error("Could not load file: " + e.getMessage());
            }
        });
    }

    /**
     * Wrapper function for opening a file.
     *
     * @param loadAction function that loads the new file
     */
    public void openFile(@NotNull final Consumer<FileManager> loadAction) {
        try {
            final var pair = editorManager.createEditor();
            final Editor editor = pair.getFirst();
            final var fm = pair.getSecond();
            loadAction.accept(fm);
            editorManager.openEditor(editor, fm);
            mimaUI.fileChanged();
            App.logger.log("loaded: " + FileName.shorten(fm.getLastFile()));
        } catch (@NotNull final IllegalArgumentException ignored) {
        } catch (@NotNull final IllegalStateException e) {
            App.logger.error(e.getMessage());
        }
    }

    /**
     * Smart save action.
     */
    public void saveSmart() {
        try {
            if (!editorManager.currentFileManager().isOnDisk()) {
                saveAs();
            } else {
                save();
            }
        } catch (@NotNull final IllegalArgumentException ignored) {
        }
    }

    /**
     * Save current file.
     */
    public void save() {
        try {
            String fileM = "Saving \""
                    + FileName.shorten(editorManager.currentFileManager().getLastFile()) + "\"";
            LoadingIndicator.start(fileM, 3);
            editorManager.currentFileManager().save();
            LoadingIndicator.stop(fileM + " (done)");
        } catch (@NotNull final IllegalArgumentException | IOException e) {
            LoadingIndicator.error("Saving failed: " + e.getMessage());
        }
    }

    /**
     * Save current file and choose file name.
     */
    public void saveAs() {
        editorManager.currentFileManager().saveAs();
        mimaUI.fileChanged();
    }

}
