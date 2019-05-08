package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Utility class for manipulating file names.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class FileName {

    private static final int MAX_FILE_DISPLAY_LENGTH = 45;

    @Contract(" -> fail")
    private FileName() {
        assert false : "utility class constructor";
    }

    /**
     * Shortens fileName. See {@link #shorten(String, int) shorten(fileName, maxLength}. Uses {@link
     * #MAX_FILE_DISPLAY_LENGTH} as parameter.
     *
     * @param fileName Name to shorten
     * @return shortened String
     */
    public static String shorten(final String fileName) {
        return shorten(fileName, MAX_FILE_DISPLAY_LENGTH);
    }

    /**
     * Shorten a fileName to the given length. Shortens by deleting directories in the middle and
     * replacing them by \...\
     *
     * @param fileName  Name to shorten
     * @param maxLength maximum length to shorten to.
     * @return shortened String
     */
    public static String shorten(final String fileName, final int maxLength) {
        String name = fileName;
        final String[] split = name.split("\\\\");
        int indexLow = split.length / 2;
        int indexHigh = indexLow + 1;
        while (name.length() > maxLength && indexHigh < split.length && indexLow > 0) {
            final StringBuilder sb = new StringBuilder();
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
     * Remove file extension from name.
     *
     * @param file file to remove extension from.
     * @return file name without extension.
     */
    @NotNull
    public static String removeExtension(@NotNull final File file) {
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
    @NotNull
    public static String removeExtension(
            @NotNull final File file, @NotNull final String[] blacklist) {
        final String name = file.getName();
        for (final var s : blacklist) {
            if (name.endsWith("." + s)) {
                return removeExtension(file);
            }
        }
        return name;
    }

    /**
     * Escape all special characters in String.
     *
     * @param inputString input
     * @return input string with all special characters escaped
     */
    @NotNull
    public static String escapeMetaCharacters(@NotNull final String inputString) {
        final String[] metaCharacters = {
                "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%"
        };
        String input = inputString;
        for (final String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                input = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return input;
    }
}
