package edu.kit.mima.gui.loading;

import java.io.*;
import java.util.stream.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SaveHandler {

    private final String saveDirectory;
    private final String tmpFile;

    public SaveHandler(String saveDirectory) {
        this.saveDirectory = saveDirectory;
        this.tmpFile = saveDirectory + "/save.tmp";
    }

    public String loadFile(String file) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public String loadTmp() throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tmpFile), "ISO-8859-1"));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public void saveTmp(String text) throws IOException {
        PrintWriter writer = new PrintWriter(tmpFile, "ISO-8859-1");
        writer.write(text);
        writer.close();
    }

    public void deleteTmp() {
        new File(tmpFile).delete();
    }

    public void saveFile(String text, String path) throws IOException {
        PrintWriter writer = new PrintWriter(path, "ISO-8859-1");
        writer.write(text);
        writer.close();
    }
}
