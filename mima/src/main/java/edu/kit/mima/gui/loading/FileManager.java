package edu.kit.mima.gui.loading;

import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static edu.kit.mima.gui.logging.Logger.error;

/**
 * Responsible for saving/loading/creating files
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileManager implements AutoCloseable {

    private static final String UNSAVED_PREFIX = "unsaved";
    private final Component parent;

    private final FileRequester fileRequester;
    private final List<FileEventHandler> eventHandlers;

    private final String[] extensions;
    private boolean isNewFile;
    private String lastFile;
    private String directory;

    private String text;
    private String lastExtension;

    private int fileHash;

    /**
     * Create new FileManager to control loading and saving of files.
     * Keeps track:  - if files are unsaved.
     * - last used file extension
     *
     * @param parent     parent component for dialogs
     * @param extensions allowed file extensions
     */
    public FileManager(final Component parent, final String[] extensions) {
        eventHandlers = new ArrayList<>();
        this.extensions = extensions;
        this.parent = parent;

        fileRequester = new FileRequester(this.parent, new LogLoadManager() {
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
            public void afterRequest(final File chosenFile) {
                lastFile = chosenFile.getAbsolutePath();
                updateReferences(chosenFile);
            }
        });
        directory = Preferences.getInstance().readString(PropertyKey.DIRECTORY_WORKING);
    }

    /**
     * loads text from path specified in the last saved option
     */
    public void loadLastFile() {
        var pref = Preferences.getInstance();
        String fileToLoad = pref.readString(PropertyKey.LAST_FILE);
        try {
            if (fileToLoad.startsWith(UNSAVED_PREFIX)) {
                load(pref.readString(PropertyKey.DIRECTORY_MIMA) + "\\" + fileToLoad);
                isNewFile = true;
                lastFile = fileToLoad;
            } else {
                load(fileToLoad);
            }
        } catch (final IOException | NullPointerException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Load file from path user specifies
     */
    public void load() {
        text = fileRequester.requestLoad(directory, extensions, () -> {/*Do nothing*/});
        assert text != null;
        fileHash = text.hashCode();
        isNewFile = false;
        notifyHandlers(e -> e.fileLoadedEvent(lastFile));
    }

    /**
     * Load the text from given filePath
     *
     * @throws IOException if file does not exist
     */
    public void load(String filePath) throws IOException {
        text = IOTools.loadFile(filePath);
        assert text != null;
        fileHash = text.hashCode();
        lastFile = filePath;
        updateReferences(filePath);
        notifyHandlers(e -> e.fileLoadedEvent(lastFile));
    }

    /**
     * Create a new File. Requests user to chose file type
     */
    public void newFile() {
        final String response = (String) JOptionPane.showInputDialog(parent, "Choose file type", "New File",
                JOptionPane.QUESTION_MESSAGE,
                null, extensions, extensions[0]);
        if (response == null) {
            throw new IllegalArgumentException("aborted");
        }
        createNewFile(response);
    }

    private void createNewFile(String extension) {
        lastFile = UNSAVED_PREFIX + '.' + extension;
        text = "#New File\n";
        fileHash = text.hashCode();
        updateReferences(extension);
        notifyHandlers(e -> e.fileCreated(lastFile));
    }

    /**
     * Save file to last used location
     *
     * @throws IOException mey throw IOException
     */
    public void save() throws IOException {
        IOTools.saveFile(text, lastFile);
        fileHash = text.hashCode();
        notifyHandlers(e -> e.saveEvent(lastFile));
    }

    /**
     * Save file to location of users choice
     */
    public void saveAs() {
        fileRequester.requestSave(text, directory, lastExtension,
                () -> { throw new IllegalArgumentException("aborted save"); });
        isNewFile = false;
        fileHash = text.hashCode();
        notifyHandlers(e -> e.saveEvent(lastFile));
    }

    /**
     * Asks the user if he wants to save the file
     */
    public void savePopUp(Runnable abortCallback) {
        final int response = JOptionPane.showOptionDialog(parent, "Do you want to save?", "Unsaved File",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, new String[]{"Save", "Save as", "Don't save"}, "Save");
        switch (response) {
            case 0: //Save
                if (isNewFile) {
                    saveAs();
                } else {
                    try {
                        save();
                    } catch (final IOException e) {
                        error(e.getMessage());
                    }
                }
                break;
            case 1: //Save as
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
     * Get loaded text
     *
     * @return text
     */
    public String getText() {
        return text == null ? "" : text;
    }

    /**
     * Set the text used for saving
     *
     * @param text text
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Returns whether the text has been changed since tha last save
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
     * Returns whether the current file is present on disk. Whether it is saved or not
     *
     * @return true if file is on disk
     */
    public boolean isOnDisk() {
        return !isNewFile;
    }

    /**
     * Get the path to the last used file
     *
     * @return path to last file
     */
    public String getLastFile() {
        return lastFile == null ? "" : lastFile;
    }

    /**
     * Get the extension of the last used file
     *
     * @return extension of last used file
     */
    public String getLastExtension() {
        return lastExtension;
    }

    /*
     * Set the extension used by file
     */
    private void updateReferences(final File file) {
        updateReferences(file.getAbsolutePath());
    }

    /*
     * Set the extension used by loaded file
     */
    private void updateReferences(final String file) {
        for (final String s : extensions) {
            if (file.endsWith(s)) {
                lastExtension = s;
                break;
            }
        }
        var pref = Preferences.getInstance();
        isNewFile = lastFile.startsWith(UNSAVED_PREFIX);
        File lFile = new File(lastFile);
        directory = lFile.exists()
                ? lFile.getParentFile().getAbsolutePath()
                : pref.readString(PropertyKey.DIRECTORY_MIMA);
        pref.saveString(PropertyKey.DIRECTORY_WORKING, directory);
    }

    public void addFileEventHandler(FileEventHandler handler) {
        eventHandlers.add(handler);
    }

    public boolean removeFileEventHandler(FileEventHandler handler) {
        return eventHandlers.remove(handler);
    }

    private void notifyHandlers(Consumer<FileEventHandler> action) {
        for (FileEventHandler handler : eventHandlers) {
            action.accept(handler);
        }
    }

    @Override
    public void close() throws IOException {
        if (isNewFile) {
            var pref = Preferences.getInstance();
            String path = pref.readString(PropertyKey.DIRECTORY_MIMA) + "\\" + lastFile;
            pref.saveString(PropertyKey.LAST_FILE, lastFile);
            IOTools.saveFile(text, path);
        }
    }
}
