package edu.kit.mima.gui.loading;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.io.*;
import java.util.stream.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TextLoader {

    private final LoadManager manager;
    private final Component parent;


    /**
     * TextLoader to control loading and saving files as well as handling the request for file paths
     *
     * @param parent parent component for dialogs
     * @param manager underlying load manager
     */
    public TextLoader(final Component parent, final LoadManager manager) {
        super();
        this.manager = manager;
        this.parent = parent;
    }

    /**
     * Request the user to choose a file location and save the text to this location
     *
     * @param text text to save
     * @param searchPath starting directory for the file chooser
     * @param extension allowed file extension
     * @param abortHandler action to perform if request was aborted
     */
    public void requestSave(final String text, final String searchPath, final String extension, final Runnable abortHandler) {
        String path = requestPath(searchPath, new String[]{extension}, abortHandler);
        if (path == null) return;
        if (!path.endsWith(extension)) path += "." + extension;

        manager.onSave(path);
        try (final PrintWriter writer = new PrintWriter(path, "ISO-8859-1")) {
            if (text != null) writer.write(text);
        } catch (final IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterSave();
    }

    /**
     * Request the user to choose a file location and load the text from this location
     *
     * @param searchPath starting directory for the file chooser
     * @param extensions allowed file extension
     * @param abortHandler action to perform if request was aborted
     * @return loaded text
     */
    public String requestLoad(final String searchPath, final String[] extensions, final Runnable abortHandler) {
        final String path = requestPath(searchPath, extensions, abortHandler);
        if (path == null) {
            abortHandler.run();
            throw new IllegalArgumentException("path is null");
        }

        String text = null;
        manager.onLoad(path);
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), "ISO-8859-1"))) {
            text = reader.lines().collect(Collectors.joining("\n"));
        } catch (final IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterLoad();
        return text;
    }

    /*
     * Request the path form user using JFileChooser
     */
    private String requestPath(final String savedPath, final String[] extensions, final Runnable abortHandler) {
        String path = null;
        final JFileChooser chooser = new JFileChooser(savedPath);
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(extensions[0], extensions);
        chooser.setFileFilter(filter);
        final int value = chooser.showDialog(parent, "Choose File");
        if (value == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            path = file.getAbsolutePath();
            manager.afterRequest(file);
        } else {
            abortHandler.run();
        }
        return path;
    }
}
