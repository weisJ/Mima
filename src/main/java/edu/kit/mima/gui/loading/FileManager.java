package edu.kit.mima.gui.loading;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.io.IOException;

import static edu.kit.mima.gui.logging.Logger.error;

/**
 * Responsible for saving/loading/creating files
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileManager implements AutoCloseable {

    private final Component parent;

    private final TextLoader textLoader;
    private final OptionsHandler optionsHandler;
    private final SaveHandler saveHandler;

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
     * @param parent        parent component for dialogs
     * @param saveDirectory path to saveDirectory
     * @param extensions    allowed file extensions
     */
    public FileManager(final Component parent, final String saveDirectory, final String[] extensions) {
        optionsHandler = new OptionsHandler(saveDirectory);
        saveHandler = new SaveHandler(saveDirectory);
        this.extensions = extensions;
        this.parent = parent;

        textLoader = new TextLoader(this.parent, new LogLoadManager() {
            @Override
            public void afterRequest(final File chosenFile) {
                setLastExtension(chosenFile);
                lastFile = chosenFile.getAbsolutePath();
                directory = chosenFile.getParentFile().getAbsolutePath();
            }
        });
    }

    /*
     * load last used directory from options
     */
    private void loadOptions() {
        try {
            final String[] options = optionsHandler.loadOptions();
            lastFile = options[0];
            directory = new File(lastFile).getParentFile().getAbsolutePath();
        } catch (final IOException | NullPointerException e) {
            directory = System.getProperty("user.home");
        }
    }

    /*
     * Save the last used directory to the options
     */
    private void saveOptions() throws IOException {
        optionsHandler.saveOptions(lastFile);
    }

    /**
     * loads text from path specified in the last saved option
     */
    public void loadLastFile() {
        try {
            loadOptions();
            if (lastFile.startsWith("unsaved.")) {
                createNewFile();
                setLastExtension(lastFile.split(".", 2)[1]);
            } else {
                text = saveHandler.loadFile(lastFile);
                setLastExtension(lastFile);
                fileHash = text.hashCode();
                isNewFile = false;
            }
        } catch (final IOException | NullPointerException e) {
            firstFile();
        }
    }

    /**
     * Load the text form last used file path
     */
    public void load() {
        text = textLoader.requestLoad(directory, extensions, () -> { });
        assert text != null;
        fileHash = text.hashCode();
        isNewFile = false;
    }

    /*
     * Launches a request to load/create a file
     */
    private void firstFile() {
        try {
            final int response = JOptionPane
                    .showOptionDialog(parent, "Create/Load File", "Create/Load File", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null, new String[]{"Load", "New"}, "Load");
            switch (response) {
                case 0:  //Load
                    load();
                    break;
                case 1:  //New
                    newFile();
                    break;
                default:  //Abort
                    System.exit(0);
            }
        } catch (final IllegalArgumentException ignored) { }
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
        lastFile = "unsaved." + response;
        setLastExtension(response);
        createNewFile();
    }

    private void createNewFile() {
        text = "#New File\n";
        isNewFile = true;
        fileHash = text.hashCode();
    }

    /**
     * Save file to last used location
     *
     * @throws IOException mey throw IOException
     */
    public void save() throws IOException {
        saveHandler.saveFile(text, lastFile);
        fileHash = text.hashCode();
    }

    /**
     * Save file to location of users choice
     */
    public void saveAs() {
        textLoader.requestSave(text, directory, lastExtension,
                () -> { throw new IllegalArgumentException("aborted save"); });
        isNewFile = false;
        fileHash = text.hashCode();
    }

    /**
     * Asks the user if he wants to save the file
     */
    public void savePopUp() {
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
                break;
            default:
                throw new IllegalArgumentException("aborted");
        }
    }

    /**
     * Get loaded text
     *
     * @return text
     */
    public String getText() {
        return text;
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
    public boolean isSaved() {
        if (text == null) {
            return true;
        }
        return !isNewFile && (fileHash == text.hashCode());
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
        return lastFile;
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
    private void setLastExtension(final File file) {
        setLastExtension(file.getAbsolutePath());
    }

    /*
     * Set the extension used by loaded file
     */
    private void setLastExtension(final String file) {
        for (final String s : extensions) {
            if (file.endsWith(s)) {
                lastExtension = s;
            }
        }
    }

    @Override
    public void close() throws IOException {
        saveOptions();
    }
}
