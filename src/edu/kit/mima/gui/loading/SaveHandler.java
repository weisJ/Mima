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

    /**
     * Handler to manage file saving and loading from known paths
     *
     * @param saveDirectory save directory for temporary file
     */
    public SaveHandler(String saveDirectory) {
        this.saveDirectory = saveDirectory;
        this.tmpFile = saveDirectory + "/save.tmp";
    }

    /**
     * Load file from path
     *
     * @param file file path
     * @return content of file
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    public String loadFile(String file) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    /**
     * Save to file
     *
     * @param text text to save in file
     * @param path path to file
     * @throws IOException may throw IOException during saving process
     */
    public void saveFile(String text, String path) throws IOException {
        PrintWriter writer = new PrintWriter(path, "ISO-8859-1");
        writer.write(text);
        writer.close();
    }

    /**
     * Load from default temporary file
     *
     * @return content of temporary
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    public String loadTmp() throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tmpFile), "ISO-8859-1"));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    /**
     * Save to default temporary file
     *
     * @param text text to save in file
     * @throws IOException may throw IOException during saving process
     */
    public void saveTmp(String text) throws IOException {
        PrintWriter writer = new PrintWriter(tmpFile, "ISO-8859-1");
        writer.write(text);
        writer.close();
    }

    /**
     * Delete the temporary file
     */
    public void deleteTmp() {
        new File(tmpFile).delete();
    }
}
