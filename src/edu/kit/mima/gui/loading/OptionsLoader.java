package edu.kit.mima.gui.loading;

import java.io.*;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class OptionsLoader {

    private final String optionsDirectory;
    private final String optionsPath;

    public OptionsLoader(String optionsDirectory) {
        this.optionsDirectory = optionsDirectory;
        this.optionsPath = optionsDirectory + "/.options";
        createDirectory();
    }

    private void createDirectory() {
        File homeDir = new File(optionsPath);
        if (!homeDir.exists()) {
            homeDir.mkdir();
        }
    }

    public String loadOptions() throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(optionsPath), "ISO-8859-1"));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public void saveOptions(String save) throws IOException {
        PrintWriter writer = new PrintWriter(optionsPath, "ISO-8859-1");
        writer.write(save);
        writer.close();
    }
}
