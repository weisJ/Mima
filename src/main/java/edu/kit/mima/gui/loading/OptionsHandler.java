package edu.kit.mima.gui.loading;

import java.io.File;
import java.io.IOException;

/**
 * Writer/Reader for options file
 *
 * @author Jannis Weis
 * @since 2018
 */
public class OptionsHandler {

    private final String optionsPath;
    private final SaveHandler handler;

    /**
     * Load/Save options to a specific directory
     *
     * @param optionsDirectory path to options directory
     */
    public OptionsHandler(final String optionsDirectory) {
        handler = new SaveHandler(optionsDirectory);
        optionsPath = optionsDirectory + "\\.options";
        createDirectory();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectory() {
        final File homeDir = new File(optionsPath);
        if (!homeDir.exists()) {
            homeDir.getParentFile().mkdirs();
        }
    }

    /**
     * Load the options file
     *
     * @return split of options file as array
     * @throws IOException may throw IOException during loading
     */
    public String[] loadOptions() throws IOException {
        return handler.loadFile(optionsPath).split("\n");
    }

    /**
     * Save the options file
     *
     * @param text content to write to options file
     * @throws IOException may throw IOException during loading
     */
    public void saveOptions(final String text) throws IOException {
        handler.saveFile(text, optionsPath);
    }
}
