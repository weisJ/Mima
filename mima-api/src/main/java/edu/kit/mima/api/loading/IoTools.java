package edu.kit.mima.api.loading;

import org.apache.tika.parser.txt.CharsetDetector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Reader/Writer for saving and writing files as well as writing temporary files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class IoTools {

    private static final String ENCODING = System.getProperty("file.encoding");
    private static final CharsetDetector CHARSET_DETECTOR = new CharsetDetector();

    @Contract(" -> fail")
    private IoTools() {
        assert false : "utility class Constructor";
    }

    /**
     * Load file from path.
     *
     * @param path file path
     * @return content of file
     * @throws IOException may throw IOException during loading process if the file does not exist
     */
    public static String loadFile(@NotNull final String path) throws IOException {
        final var charSet = CHARSET_DETECTOR.setText(
                new BufferedInputStream(new FileInputStream(path))).detect().getName();
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), charSet))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Save to file.
     *
     * @param text text to save in file
     * @param path path to file
     * @throws IOException if file could not be saved.
     */
    public static void saveFile(@NotNull final String text, @NotNull final String path)
            throws IOException {
        try (final PrintWriter writer = new PrintWriter(path, ENCODING)) {
            writer.write(text);
        }
    }
}
