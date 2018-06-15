package edu.kit.mima.gui.loading;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;

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
     * @param parent  parent component for dialogs
     * @param manager underlying load manager
     */
    public TextLoader(final Component parent, final LoadManager manager) {
        this.manager = manager;
        this.parent = parent;
    }

    /**
     * Request the user to choose a file location and save the text to this location
     *
     * @param text         text to save
     * @param searchPath   starting directory for the file chooser
     * @param extension    allowed file extension
     * @param abortHandler action to perform if request was aborted
     */
    public void requestSave(final String text, final String searchPath, final String extension,
                            final Runnable abortHandler) {
        String path = requestPath(searchPath, new String[]{extension}, abortHandler);
        if (path == null) {
            return;
        }
        if (!path.endsWith(extension)) {
            path += '.' + extension;
        }

        manager.onSave(path);
        try (final PrintWriter writer = new PrintWriter(path, "ISO-8859-1")) {
            if (text != null) {
                writer.write(text);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterSave();
    }

    /**
     * Request the user to choose a file location and load the text from this location
     *
     * @param searchPath   starting directory for the file chooser
     * @param extensions   allowed file extension
     * @param abortHandler action to perform if request was aborted
     * @return loaded text
     */
    @SuppressWarnings("OverlyBroadCatchBlock")
    public String requestLoad(final String searchPath, final String[] extensions, final Runnable abortHandler) {
        final String path = requestPath(searchPath, extensions, abortHandler);
        if (path == null) {
            abortHandler.run();
            throw new IllegalArgumentException("path is null");
        }

        manager.onLoad(path);
        String text = null;
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
        final JFileChooser chooser = new JFileChooser(savedPath);
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(extensions[0], extensions);
        chooser.setFileFilter(filter);
        final int value = chooser.showDialog(parent, "Choose File");
        String path = null;
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
