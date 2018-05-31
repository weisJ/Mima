package edu.kit.mima.gui.loading;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TextLoader {

    private final LoadManager manager;
    private final Component parent;
    private final String extension;


    public TextLoader(Component parent, String extension, LoadManager manager) {
        this.manager = manager;
        this.parent = parent;
        this.extension = extension;
    }

    public void requestSave(String text, String searchPath, Runnable abortHandler) {
        String path = requestPath(searchPath, abortHandler);
        if (path == null) return;
        if (!path.endsWith(extension)) path += extension;

        manager.onSave(path);
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            if (text != null) writer.write(text);
        } catch (IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterSave();
    }

    public String requestLoad(String searchPath, Runnable abortHandler) {
        String path = requestPath(searchPath, abortHandler);
        if (path == null) throw new IllegalArgumentException("path is null");

        String text = null;
        manager.onLoad(path);
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), "UTF8"))) {
            text = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterLoad();
        return text;
    }

    private String requestPath(String savedPath, Runnable abortHandler) {
        String path = null;
        JFileChooser chooser = new JFileChooser(savedPath);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(extension, extension.substring(1));
        chooser.setFileFilter(filter);
        int value = chooser.showDialog(parent, "Choose File");
        if (value == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            path = file.getAbsolutePath();
            manager.afterRequest(file);
        } else {
            abortHandler.run();
        }
        return path;
    }
}
