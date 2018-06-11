package edu.kit.mima.gui;

import edu.kit.mima.gui.loading.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import static edu.kit.mima.gui.logging.Logger.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileManager implements AutoCloseable {

    private final Component parent;

    private final TextLoader textLoader;
    private final OptionsLoader optionsLoader;
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
     *               - last used file extension
     *
     * @param parent parent component for dialogs
     * @param saveDirectory path to saveDirectory
     * @param extensions allowed file extensions
     */
    public FileManager(final Component parent, final String saveDirectory, final String[] extensions) {
        super();
        optionsLoader = new OptionsLoader(saveDirectory);
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
            final String[] options = optionsLoader.loadOptions();
            this.lastFile = options[0];
            this.directory = new File(lastFile).getParentFile().getAbsolutePath();
        } catch (final IOException e) {
            this.directory = System.getProperty("user.home");
        }
    }

    /*
     * Save the last used directory to the options
     */
    private void saveOptions() throws IOException {
        optionsLoader.saveOptions(lastFile);
    }

    /**
     * loads text from path specified in the last saved option
     */
    public void loadLastFile() {
        try {
            loadOptions();
            text = saveHandler.loadFile(lastFile);
            setLastExtension(lastFile);
            fileHash = text.hashCode();
        } catch (final IOException e) {
            firstFile();
        }
    }

    /**
     * Load the text form last used file path
     */
    public void load() {
        text = textLoader.requestLoad(directory, extensions, () -> { });
        fileHash = text.hashCode();
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
            if (response == 0) { //Load
                load();
            } else if (response == 1) { //New
                newFile();
            } else { //Abort
                System.exit(0);
            }
        } catch (final IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    /**
     * Create a new File. Requests user to chose file type
     */
    public void newFile() {
        final String response = (String) JOptionPane.showInputDialog(parent, "Choose file type", "New File",
                                                                     JOptionPane.QUESTION_MESSAGE,
                                                                     null, extensions, extensions[0]);
        if (response == null) return;
        setLastExtension(response);
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
        try {
            textLoader.requestSave(text, directory, lastExtension, () -> { throw new IllegalArgumentException(); });
        } catch (final IllegalArgumentException ignored) { }
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
        if (response == 0) {
            if (isNewFile) {
                saveAs();
            } else {
                try {
                    save();
                } catch (final IOException e) {
                    error(e.getMessage());
                }
            }
        } else if (response == 1) {
            saveAs();
        } else if (response == JOptionPane.CLOSED_OPTION) {
            throw new IllegalArgumentException("Window not closed");
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
     * Get the lines array of getText()
     *
     * @return array of lines
     */
    public String[] lines() {
        return text.split("\n");
    }

    /**
     * Returns whether the text has been changed since tha last save
     *
     * @return true if file has not changed
     */
    public boolean isSaved() {
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
     * Set the extension used by loaded file
     */
    private void setLastExtension(final String file) {
        for (final String s : extensions) {
            if (file.endsWith(s)) {
                lastExtension = s;
            }
        }
    }

    /*
     * Set the extension used by file
     */
    private void setLastExtension(final File file) {
        setLastExtension(file.getAbsolutePath());
    }

    @Override
    public void close() throws IOException {
      saveOptions();
    }
}
