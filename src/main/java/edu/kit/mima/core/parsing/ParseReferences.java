package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ParseReferences {

    /**
     * path to current working directory
     */
    public static String WORKING_DIRECTORY = "";
    /**
     * Path to home directory
     */
    public static String MIMA_DIR = System.getProperty("user.home") + "\\.mima";
    /**
     * File extension for default mimax files
     */
    public static String FILE_EXTENSION_X = "mimax";
    /**
     * File extension for default mima files
     */
    public static String FILE_EXTENSION = "mima";
    /**
     * File extensions that are supported.
     */
    public static String[] FILE_EXTENSIONS = {FILE_EXTENSION, FILE_EXTENSION_X};

    private ParseReferences() {
        assert false : "Variable class constructor";
    }
}
