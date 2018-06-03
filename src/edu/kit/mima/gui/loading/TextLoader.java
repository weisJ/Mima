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


    public TextLoader(Component parent, LoadManager manager) {
        this.manager = manager;
        this.parent = parent;
    }

    public void requestSave(String text, String searchPath, String extension, Runnable abortHandler) {
        String path = requestPath(searchPath, new String[]{extension}, abortHandler);
        if (path == null) return;
        if (!path.endsWith(extension)) path += "." + extension;

        manager.onSave(path);
        try (final PrintWriter writer = new PrintWriter(path, "ISO-8859-1")) {
            if (text != null) writer.write(text);
        } catch (IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterSave();
    }

    public String requestLoad(String searchPath, String[] extensions, Runnable abortHandler) {
        String path = requestPath(searchPath, extensions, abortHandler);
        if (path == null) {
            abortHandler.run();
            throw new IllegalArgumentException("path is null");
        }

        String text = null;
        manager.onLoad(path);
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), "ISO-8859-1"))) {
            text = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            manager.onFail(e.getMessage());
        }
        manager.afterLoad();
        return text;
    }

    private String requestPath(String savedPath, String[] extensions, Runnable abortHandler) {
        String path = null;
        JFileChooser chooser = new JFileChooser(savedPath);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(extensions[0], extensions);
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
