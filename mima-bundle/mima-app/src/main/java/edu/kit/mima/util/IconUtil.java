package edu.kit.mima.util;

import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.core.MimaConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class IconUtil {

    private IconUtil() {

    }

    /**
     * Get the appropriate icon for a file.
     *
     * @param file file to get icon for.
     * @return icon associated with the given file.
     */
    @NotNull
    public static Icon forFile(@NotNull final File file) {
        final String name = file.getName();
        if (file.isDirectory()) {
            return Icons.FOLDER;
        } else if (name.endsWith("." + MimaConstants.MIMA_EXTENSION)) {
            return Icons.MIMA;
        } else if (name.endsWith("." + MimaConstants.MIMA_X_EXTENSION)) {
            return Icons.MIMA_X;
        } else if (!name.contains(".")) {
            return Icons.UNKNOWN_FILE;
        } else {
            String fileType = null;
            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (@NotNull final IOException ignored) {
            }
            if (fileType != null && fileType.startsWith("text")) {
                return Icons.TEXT_FILE;
            } else {
                return Icons.GENERAL_FILE;
            }
        }
    }

    /**
     * Get the appropriate icon for a file from filepath.
     *
     * @param name path of file.
     * @return icon associated with the given file.
     */
    @NotNull
    public static Icon forFile(@NotNull final String name) {
        return forFile(new File(name));
    }
}
