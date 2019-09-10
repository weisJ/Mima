package edu.kit.mima.api.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

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
            if (FilenameUtils.isExtension(name, s)) {
                return FilenameUtils.removeExtension(name);
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

    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     * <p>This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash and before the last dot is returned.
     * <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * <p>The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists. Null bytes inside string
     * will be removed
     */
    public static String getBaseName(final String filename) {
        return FilenameUtils.getBaseName(filename);
    }

    /**
     * Checks whether the extension of the filename is that specified.
     * <p>This method obtains the extension as the textual part of the filename
     * after the last dot. There must be no directory separator after the dot.
     * The extension check is case-sensitive on all platforms.
     *
     * @param filename  the filename to query, null returns false
     * @param extension the extension to check for, null or empty checks for no extension
     * @return true if the filename has the specified extension
     * @throws java.lang.IllegalArgumentException if the supplied filename contains null bytes
     */
    public static boolean isExtension(final String filename, final String extension) {
        return FilenameUtils.isExtension(filename, extension);
    }

    /**
     * Removes the extension from a filename.
     * <p>This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt    --&gt; foo
     * a\b\c.jpg  --&gt; a\b\c
     * a\b\c      --&gt; a\b\c
     * a.b\c      --&gt; a.b\c
     * </pre>
     * <p>The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to query, null returns null
     * @return the filename minus the extension
     */
    public static String removeExtension(final String filename) {
        return FilenameUtils.removeExtension(filename);
    }

    /**
     * Gets the extension of a filename.
     * <p>This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt      --&gt; "txt"
     * a/b/c.jpg    --&gt; "jpg"
     * a/b.txt/c    --&gt; ""
     * a/b/c        --&gt; ""
     * </pre>
     * <p>The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists or {@code null}
     * if the filename is {@code null}.
     */
    public static String getExtension(final String filename) {
        return FilenameUtils.getExtension(filename);
    }

    /**
     * Check if the given path resembles a valid path on the machine.
     *
     * @param path the path to check.
     * @return true if valid. The path must not exist for this to evaluate to true but has to be valid.
     */
    public static boolean isValidPath(final String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException e) {
            return false;
        }
        return true;
    }


    /**
     * Check if given name is a valid file/directory name on this machine.
     *
     * @param name name of file.
     * @return true if valid.
     */
    public static boolean isValidFileName(final String name) {
        return isValidPath(FilenameUtils.concat(FileUtils.getUserDirectoryPath(), name));
    }

}
