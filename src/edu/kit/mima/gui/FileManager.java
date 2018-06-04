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
public class FileManager {

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

    public FileManager(Component parent, String saveDirectory, String[] extensions) {
        optionsLoader = new OptionsLoader(saveDirectory);
        saveHandler = new SaveHandler(saveDirectory);
        this.extensions = extensions;
        this.parent = parent;

        textLoader = new TextLoader(this.parent, new LogLoadManager() {
            @Override
            public void afterRequest(File chosenFile) {
                setLastExtension(chosenFile);
                lastFile = chosenFile.getAbsolutePath();
                directory = chosenFile.getParentFile().getAbsolutePath();
            }
        });
    }

    public void loadOptions() {
        try {
            String[] options = optionsLoader.loadOptions();
            this.lastFile = options[0];
            this.directory = new File(lastFile).getParentFile().getAbsolutePath();
        } catch (IOException e) {
            this.directory = System.getProperty("user.home");
        }
    }

    public void saveOptions() throws IOException {
        optionsLoader.saveOptions(lastFile);
    }

    public void loadLastFile() {
        try {
            text = saveHandler.loadFile(lastFile);
            setLastExtension(lastFile);
            fileHash = text.hashCode();
        } catch (IOException e) {
            firstStart();
        }
    }

    public void load() {
        text = textLoader.requestLoad(directory, extensions, () -> { });
        fileHash = text.hashCode();
    }

    private void firstStart() {
        try {
            int response = JOptionPane
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
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    public void newFile() {
        int response = JOptionPane.showOptionDialog(parent, "Choose file type", "File type",
                                                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                                    null, extensions, extensions[0]);
        if (response == JOptionPane.CLOSED_OPTION) return;
        setLastExtension(extensions[response]);
        text = "#Put code here\n";
        isNewFile = true;
        fileHash = text.hashCode();
    }

    public void save() throws IOException {
        saveHandler.saveFile(text, lastFile);
        fileHash = text.hashCode();
    }

    public void saveAs() {
        try {
            textLoader.requestSave(text, directory, lastExtension,
                                                    () -> { throw new IllegalArgumentException(); });
        } catch (IllegalArgumentException ignored) { }
        isNewFile = false;
        fileHash = text.hashCode();
    }

    public void savePopUp() {
        int response = JOptionPane.showOptionDialog(parent, "Do you want to save?", "Unsaved File",
                                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                                    null, new String[]{"Save", "Save as", "Don't save"}, "Save");
        if (response == 0) {
            if (isNewFile) {
                saveAs();
            } else {
                try {
                    save();
                } catch (IOException e) {
                    error(e.getMessage());
                }
            }
        } else if (response == 1) {
            saveAs();
        } else if (response == JOptionPane.CLOSED_OPTION) {
            throw new IllegalArgumentException("Window not closed");
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String[] lines() {
        return text.split("\n");
    }

    public boolean isSaved() {
        return !isNewFile && fileHash == text.hashCode();
    }

    public boolean isOnDisk() {
        return !isNewFile;
    }

    public String getLastFile() {
        return lastFile;
    }

    public String getLastExtension() {
        return lastExtension;
    }

    private void setLastExtension(String file) {
        for (String s : extensions) {
            if (file.endsWith(s)) {
                lastExtension = s;
            }
        }
    }

    private void setLastExtension(File file) {
        setLastExtension(file.getAbsolutePath());
    }
}
