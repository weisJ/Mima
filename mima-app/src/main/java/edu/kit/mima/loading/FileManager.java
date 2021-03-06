package edu.kit.mima.loading;

import edu.kit.mima.api.loading.FileEventHandler;
import edu.kit.mima.api.loading.FileRequester;
import edu.kit.mima.api.loading.IoTools;
import edu.kit.mima.api.util.FileName;
import edu.kit.mima.gui.components.dialog.FileDialog;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Responsible for saving/loading/creating files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileManager implements AutoCloseable {

    private final Component parent;

    @NotNull
    private final FileRequester fileRequester;
    @NotNull
    private final List<FileEventHandler> eventHandlers;

    private final String[] extensions;
    private boolean isNewFile;
    private String lastFile;
    private String directory;

    @Nullable
    private String text;
    private String lastExtension;

    private int fileHash;

    /**
     * Create new FileManager to control loading and saving of files. Keeps track:
     *
     * <ul>
     * <li>if files are unsaved
     * <li>last used file extension
     * </ul>
     *
     * @param parent     parent component for dialogs
     * @param extensions allowed file extensions
     */
    public FileManager(final Component parent, final String[] extensions) {
        eventHandlers = new ArrayList<>();
        this.extensions = extensions;
        this.parent = parent;

        fileRequester = new FileRequester(
                this.parent,
                new LogLoadManager() {
                    private String backupLastFile;
                    private String backupLastExtension;

                    @Override
                    public void beforeLoad() {
                        backupLastFile = lastFile;
                        backupLastExtension = lastExtension;
                    }

                    @Override
                    public void onFail(final String errorMessage) {
                        super.onFail(errorMessage);
                        lastFile = backupLastFile;
                        lastExtension = backupLastExtension;
                    }

                    @Override
                    public void afterRequest(@NotNull final File chosenFile) {
                        lastFile = chosenFile.getAbsolutePath();
                        updateReferences();
                    }
                });
        directory = Preferences.getInstance().readString(PropertyKey.DIRECTORY_WORKING);
    }

    /**
     * Load file from path user specifies.
     *
     * @throws IOException if no file was chosen or file could not be loaded.
     */
    public void load() throws IOException {
        text = fileRequester.requestLoad(directory, extensions, () -> {
        });
        if (text == null) {
            throw new IOException("no file given");
        }
        fileHash = text.hashCode();
        isNewFile = false;
        notifyHandlers(e -> e.fileLoadedEvent(lastFile));
    }

    /**
     * Load the text from given filePath.
     *
     * @param filePath path of file
     * @throws IOException if file does not exist
     */
    public void load(@NotNull final String filePath) throws IOException {
        text = IoTools.loadFile(filePath);
        fileHash = text.hashCode();
        lastFile = filePath;
        updateReferences();
        notifyHandlers(e -> e.fileLoadedEvent(lastFile));
    }

    /**
     * Create a new File. Requests user to chose file type
     */
    public void newFile() {
        final String response = (String) JOptionPane.showInputDialog(
                                parent,
                                "Choose file type",
                                "New File",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                extensions,
                                extensions[0]);
        final File file = FileDialog.showFileDialog(parent,
                                                    new File(Preferences.getInstance()
                                                                     .readString(PropertyKey.DIRECTORY_MIMA)),
                                                    response);
        try {
            if (file.createNewFile()) {
                createNewFile(file);
            } else {
                JOptionPane.showMessageDialog(parent, "Couldn't create File", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                throw new IllegalArgumentException("Couldn't create file");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Couldn't create file");
        }
    }

    private void createNewFile(final File file) {
        lastFile = file.getAbsolutePath();
        text = "";
        fileHash = text.hashCode();
        text = "#New File\n";
        updateReferences();
        notifyHandlers(e -> e.fileCreated(lastFile));
    }

    /**
     * Save file to last used location.
     *
     * @throws IOException mey throw IOException
     */
    public void save() throws IOException {
        if (text == null) {
            return;
        }
        IoTools.saveFile(text, lastFile);
        fileHash = text.hashCode();
        notifyHandlers(e -> e.saveEvent(lastFile));
    }

    /**
     * Save file to location of users choice.
     */
    public void saveAs() {
        if (text == null) {
            return;
        }
        fileRequester.requestSave(text,
                                  directory,
                                  lastExtension,
                                  () -> {
                                      throw new IllegalArgumentException("aborted save");
                                  });
        isNewFile = false;
        fileHash = text.hashCode();
        notifyHandlers(e -> e.saveEvent(lastFile));
    }

    /**
     * Asks the user if he wants to save the file.
     *
     * @param abortCallback callback in case of abort.
     */
    public void savePopUp(@NotNull final Runnable abortCallback) {
        final int response =
                JOptionPane.showOptionDialog(
                        parent,
                        "Do you want to save?",
                        "Unsaved File",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[]{"Save", "Save as", "Don't save"},
                        "Save");
        switch (response) {
            case 0: // Save
                if (isNewFile) {
                    saveAs();
                } else {
                    try {
                        save();
                    } catch (@NotNull final IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1: // Save as
                saveAs();
                break;
            case 2: /*Don't save (do nothing)*/
                isNewFile = false;
                break;
            default:
                abortCallback.run();
                break;
        }
    }

    /**
     * Get loaded text.
     *
     * @return text
     */
    @NotNull
    public String getText() {
        return text == null ? "" : text;
    }

    /**
     * Set the text used for saving.
     *
     * @param text text
     */
    public void setText(@Nullable final String text) {
        this.text = text;
    }

    /**
     * Returns whether the text has been changed since tha last save.
     *
     * @return true if file has not changed
     */
    public boolean unsaved() {
        if (text == null) {
            return false;
        }
        return isNewFile || fileHash != text.hashCode();
    }

    /**
     * Returns whether the current file is present on disk. Whether it is saved or not.
     *
     * @return true if file is on disk
     */
    public boolean isOnDisk() {
        return !isNewFile;
    }

    /**
     * Get the path to the last used file.
     *
     * @return path to last file
     */
    @NotNull
    public String getLastFile() {
        return Optional.ofNullable(lastFile).orElse("");
    }

    /*
     * Set the extension used by loaded file
     */
    private void updateReferences() {
        final var pref = Preferences.getInstance();
        final File lFile = new File(lastFile);
        lastExtension = FileName.getExtension(lFile.getName());
        directory = lFile.getParentFile().getAbsolutePath();
        pref.saveString(PropertyKey.DIRECTORY_WORKING, directory);
    }

    /**
     * Add {@link FileEventHandler}.
     *
     * @param handler handler to add.
     */
    public void addFileEventHandler(final FileEventHandler handler) {
        eventHandlers.add(handler);
    }

    /**
     * Remove {@link FileEventHandler}.
     *
     * @param handler handler to remove
     */
    public void removeFileEventHandler(final FileEventHandler handler) {
        eventHandlers.remove(handler);
    }

    private void notifyHandlers(@NotNull final Consumer<FileEventHandler> action) {
        for (final FileEventHandler handler : eventHandlers) {
            action.accept(handler);
        }
    }

    @Override
    public void close() throws IOException {
        if (isNewFile && text != null) {
            final var pref = Preferences.getInstance();
            final String path = pref.readString(PropertyKey.DIRECTORY_MIMA) + "\\" + lastFile;
            pref.saveString(PropertyKey.LAST_FILE, lastFile);
            IoTools.saveFile(text, path);
        }
    }
}
