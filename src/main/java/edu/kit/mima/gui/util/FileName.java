package edu.kit.mima.gui.util;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class FileName {

    private FileName() {
        assert false : "utility class constructor";
    }

    public static String shorten(String fileName, int maxLength) {
        if (fileName.length() > maxLength) {
            return fileName.substring(0, fileName.indexOf('\\') + 1)
                    + "..."
                    + fileName.substring(fileName.lastIndexOf('\\'));
        } else {
            return fileName;
        }
    }
}
