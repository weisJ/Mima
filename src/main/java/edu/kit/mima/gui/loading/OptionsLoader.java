package edu.kit.mima.gui.loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class OptionsLoader {

    private final String optionsPath;

    /**
     * Load/Save options to a specific directory
     *
     * @param optionsDirectory path to options directory
     */
    public OptionsLoader(final String optionsDirectory) {
        optionsPath = optionsDirectory + "/.options";
        createDirectory();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectory() {
        final File homeDir = new File(optionsPath);
        if (!homeDir.exists()) {
            homeDir.mkdir();
        }
    }

    /**
     * Load the options file
     *
     * @return lines of options file as array
     * @throws IOException may throw IOException during loading
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public String[] loadOptions() throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(optionsPath), "ISO-8859-1"))) {
            return reader.lines().toArray(String[]::new);
        }
    }

    /**
     * Save the options file
     *
     * @param save content to write to options file
     * @throws IOException may throw IOException during loading
     */
    @SuppressWarnings("OverlyBroadThrowsClause")
    public void saveOptions(final String save) throws IOException {
        try (final PrintWriter writer = new PrintWriter(optionsPath, "ISO-8859-1")) {
            writer.write(save);
        }
    }
}
