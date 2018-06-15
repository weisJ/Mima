package edu.kit.mima.gui.loading;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Reader/Writer for saving and writing files as well as writing temporary files
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SaveHandler {

    private final String tmpFile;

    /**
     * Handler to manage file saving and loading from known paths
     *
     * @param saveDirectory save directory for temporary file
     */
    public SaveHandler(final String saveDirectory) {
        tmpFile = saveDirectory + "/save.tmp";
    }

    /**
     * Load file from path
     *
     * @param path file path
     * @return content of file
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public String loadFile(final String path) throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), "ISO-8859-1"))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Save to file
     *
     * @param text text to save in file
     * @param path path to file
     * @throws IOException may throw IOException during saving process
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public void saveFile(final String text, final String path) throws IOException {
        try (final PrintWriter writer = new PrintWriter(path, "ISO-8859-1")) {
            writer.write(text);
        }
    }

    /**
     * Load from default temporary file
     *
     * @return content of temporary
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public String loadTmp() throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tmpFile), "ISO-8859-1"))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Save to default temporary file
     *
     * @param text text to save in file
     * @throws IOException may throw IOException during saving process
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public void saveTmp(final String text) throws IOException {
        try (final PrintWriter writer = new PrintWriter(tmpFile, "ISO-8859-1")) {
            writer.write(text);
        }
    }

    /**
     * Delete the temporary file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteTmp() {
        new File(tmpFile).delete();
    }
}
