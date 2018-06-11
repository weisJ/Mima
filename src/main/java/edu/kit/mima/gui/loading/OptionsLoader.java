package edu.kit.mima.gui.loading;

import java.io.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class OptionsLoader {

    private final String optionsDirectory;
    private final String optionsPath;

    /**
     * Load/Save options to a specific directory
     *
     * @param optionsDirectory path to options directory
     */
    public OptionsLoader(final String optionsDirectory) {
        super();
        this.optionsDirectory = optionsDirectory;
        this.optionsPath = optionsDirectory + "/.options";
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
    public String[] loadOptions() throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(optionsPath), "ISO-8859-1"));
        return reader.lines().toArray(String[]::new);
    }

    /**
     * Save the options file
     *
     * @param save content to write to options file
     * @throws IOException may throw IOException during loading
     */
    public void saveOptions(final String save) throws IOException {
        final PrintWriter writer = new PrintWriter(optionsPath, "ISO-8859-1");
        writer.write(save);
        writer.close();
    }
}
