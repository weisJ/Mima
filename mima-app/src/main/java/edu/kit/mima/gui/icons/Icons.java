package edu.kit.mima.gui.icons;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.core.MimaConstants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Icons class for the App. Contains all used icons.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Icons {
    private static final Icons instance = new Icons();
    public static final Icon BREAKPOINT = loadIcon("breakpoint.svg");

    public static final Icon DEBUG_INACTIVE = loadIcon("debug_inactive.svg");
    public static final Icon DEBUG = loadIcon("debug.svg");
    public static final Icon DEBUG_ACTIVE = loadIcon("debug_active.svg");

    public static final Icon RUN_INACTIVE = loadIcon("run_inactive.svg");
    public static final Icon RUN = loadIcon("run.svg");
    public static final Icon RUN_ACTIVE = loadIcon("run_active.svg");

    public static final Icon PAUSE = loadIcon("pause.svg");
    public static final Icon PAUSE_INACTIVE = loadIcon("pause_inactive.svg");
    public static final Icon RESUME = loadIcon("resume.svg");
    public static final Icon RESUME_INACTIVE = loadIcon("resume_inactive.svg");
    public static final Icon STOP = loadIcon("stop.svg");
    public static final Icon STOP_INACTIVE = loadIcon("stop_inactive.svg");

    public static final Icon DIVIDER = loadIcon("divider.svg");
    public static final Icon FOLDER = loadIcon("folder.svg");
    public static final Icon FOLDER_ROOT = loadIcon("folder_root.svg");
    public static final Icon MIMA = loadIcon("class.svg");
    public static final Icon MIMA_X = loadIcon("class.svg");
    public static final Icon TEXT_FILE = loadIcon("text.svg");
    public static final Icon UNKNOWN_FILE = loadIcon("unknown.svg");

    public static final Icon MORE_TABS = loadIcon("moreTabs.svg", 20, 20);

    public static final Icon REDO = loadIcon("redo.svg");
    public static final Icon REDO_INACTIVE = loadIcon("redo_inactive.svg");
    public static final Icon UNDO = loadIcon("undo.svg");
    public static final Icon UNDO_INACTIVE = loadIcon("undo_inactive.svg");

    public static final Icon MEMORY = loadIcon("memory.svg");
    public static final Icon CONSOLE = loadIcon("console.svg");

    public static final Icon SEARCH = loadIcon("search.svg");
    public static final Icon SEARCH_WITH_HISTORY = loadIcon("searchWithHistory.svg");
    public static final Icon CLOSE = loadIcon("close.svg");
    public static final Icon CLOSE_HOVER = loadIcon("closeHovered.svg");
    public static final Icon CLEAR = loadIcon("clear.svg", 12, 12);
    public static final Icon COLLAPSE = loadIcon("collapse.svg");

    private Icons() {
    }

    @NotNull
    private static Icon loadIcon(@NotNull final String name) {
        return loadIcon(name, 16, 16);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private static Icon loadIcon(@NotNull final String name, final int w, final int h) {
        try {
            return new SVGIcon(Objects.requireNonNull(instance.getClass().getResource(name)), w, h);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EmptyIcon(0, 0);
    }

    public static void loadIcons() {
        System.out.println("Loaded Icons");
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
            return FOLDER;
        } else if (name.endsWith("." + MimaConstants.MIMA_EXTENSION)) {
            return MIMA;
        } else if (name.endsWith("." + MimaConstants.MIMA_X_EXTENSION)) {
            return MIMA_X;
        } else {
            String fileType = null;
            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (@NotNull final IOException ignored) {
            }
            if (fileType != null && fileType.startsWith("text")) {
                return TEXT_FILE;
            } else {
                return UNKNOWN_FILE;
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
