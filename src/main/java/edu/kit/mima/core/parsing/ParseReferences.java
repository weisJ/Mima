package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ParseReferences {

    /**
     * Path to home directory
     */
    public static final String MIMA_DIR = System.getProperty("user.home") + "\\.mima";
    /**
     * File extension for default mimax files
     */
    public static final String FILE_EXTENSION_X = "mimax";
    /**
     * File extension for default mima files
     */
    public static final String FILE_EXTENSION = "mima";
    /**
     * File extensions that are supported.
     */
    public static final String[] FILE_EXTENSIONS = {FILE_EXTENSION, FILE_EXTENSION_X};
    /**
     * path to current working directory
     */
    public static String WORKING_DIRECTORY = "";

    private ParseReferences() {
        assert false : "Variable class constructor";
    }
}
