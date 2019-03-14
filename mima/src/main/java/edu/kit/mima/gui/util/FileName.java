package edu.kit.mima.gui.util;

import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class FileName {

    private static final int MAX_FILE_DISPLAY_LENGTH = 45;

    private FileName() {
        assert false : "utility class constructor";
    }

    /**
     * Shortens fileName. See {@link #shorten(String, int) shorten(fileName, maxLength}.
     * Uses {@link #MAX_FILE_DISPLAY_LENGTH} as parameter.
     *
     * @param fileName Name to shorten
     * @return shortened String
     */
    public static String shorten(String fileName) {
        return shorten(fileName, MAX_FILE_DISPLAY_LENGTH);
    }

    /**
     * Shorten a fileName to the given length.
     * Shortens by deleting directories in the middle and replacing them by \...\
     *
     * @param fileName  Name to shorten
     * @param maxLength maximum length to shorten to.
     * @return shortened String
     */
    public static String shorten(String fileName, int maxLength) {
        String name = fileName;
        String[] split = name.split("\\\\");
        int indexLow = split.length / 2;
        int indexHigh = indexLow + 1;
        while (name.length() > maxLength && indexHigh < split.length && indexLow > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indexLow; i++) {
                sb.append(split[i]).append('\\');
            }
            sb.append("...\\");
            for (int i = indexHigh; i < split.length; i++) {
                sb.append(split[i]);
                if (i != split.length - 1) {
                    sb.append('\\');
                }
            }
            name = sb.toString();
            indexHigh++;
            indexLow--;
        }
        return name;
    }

    /**
     * Remove file extension from name
     *
     * @param file file to remove extension from.
     * @return file name without extension.
     */
    public static String removeExtension(File file) {
        if (!file.isDirectory() && file.getName().contains(".")) {
            return file.getName().substring(0, Math.max(file.getName().lastIndexOf('.'), 0));
        } else {
            return file.getName();
        }
    }

    /**
     * Remove specified extensions from file.
     *
     * @param file      file to remove extension from
     * @param blacklist extensions to remove
     * @return file without extension.
     */
    public static String removeExtension(File file, String[] blacklist) {
        String name = file.getName();
        for (var s : blacklist) {
            if (name.endsWith("." + s)) {
                return removeExtension(file);
            }
        }
        return name;
    }

    /**
     * Escape all special characters in String
     *
     * @param inputString input
     * @return input string with all special characters escaped
     */
    public static String escapeMetaCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%"};

        for (String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }
}
