package edu.kit.mima.gui.loading;

import org.apache.tika.parser.txt.CharsetDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Reader/Writer for saving and writing files as well as writing temporary files
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class IOTools {

    private static final String ENCODING = System.getProperty("file.encoding");
    private static final CharsetDetector charsetDetector = new CharsetDetector();

    private IOTools() {
        assert false : "utility class Constructor";
    }

    /**
     * Load file from path
     *
     * @param path file path
     * @return content of file
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public static String loadFile(final String path) throws IOException {
        var charSet = charsetDetector.setText(new BufferedInputStream(new FileInputStream(path))).detect().getName();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), charSet))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Save to file
     *
     * @param text text to save in file
     * @param path path to file
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public static void saveFile(final String text, final String path) throws IOException {
        try (final PrintWriter writer = new PrintWriter(path, ENCODING)) {
            writer.write(text);
        }
    }
}
