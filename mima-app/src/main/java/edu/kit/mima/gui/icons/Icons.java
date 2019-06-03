package edu.kit.mima.gui.icons;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.core.MimaConstants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

    public static final Icon BREAKPOINT = create("breakpoint.svg");

    public static final UIAwareIcon DEBUG_INACTIVE = create("debug_inactive.svg");
    public static final UIAwareIcon DEBUG = create("debug.svg");
    public static final UIAwareIcon DEBUG_ACTIVE = create("debug_active.svg");
    public static final UIAwareIcon RUN_INACTIVE = create("run_inactive.svg");
    public static final UIAwareIcon RUN = create("run.svg");
    public static final UIAwareIcon RUN_ACTIVE = create("run_active.svg");
    public static final UIAwareIcon PAUSE = create("pause.svg");
    public static final UIAwareIcon PAUSE_INACTIVE = create("pause_inactive.svg");
    public static final UIAwareIcon RESUME = create("resume.svg");
    public static final UIAwareIcon RESUME_INACTIVE = create("resume_inactive.svg");
    public static final UIAwareIcon STOP = create("stop.svg");
    public static final UIAwareIcon STOP_INACTIVE = create("stop_inactive.svg");
    public static final UIAwareIcon DIVIDER = create("divider.svg");
    public static final Icon STACK_POINTER = create("stack_pointer.svg");

    public static final UIAwareIcon FOLDER = create("folder.svg");
    public static final UIAwareIcon FOLDER_ROOT = create("folder_root.svg");
    public static final UIAwareIcon MIMA = create("class.svg");
    public static final UIAwareIcon MIMA_X = create("class.svg");
    public static final UIAwareIcon TEXT_FILE = create("text.svg");
    public static final UIAwareIcon UNKNOWN_FILE = create("unknown.svg");

    public static final UIAwareIcon REDO = create("redo.svg");
    public static final UIAwareIcon REDO_INACTIVE = create("redo_inactive.svg");
    public static final UIAwareIcon UNDO = create("undo.svg");
    public static final UIAwareIcon UNDO_INACTIVE = create("undo_inactive.svg");

    public static final UIAwareIcon MEMORY = create("memory.svg");
    public static final UIAwareIcon CONSOLE = create("console.svg");

    public static final UIAwareIcon SEARCH = create("search.svg");
    public static final UIAwareIcon SEARCH_WITH_HISTORY = create("searchWithHistory.svg");
    public static final UIAwareIcon CLOSE = create("close.svg");
    public static final UIAwareIcon CLOSE_HOVER = create("closeHovered.svg");
    public static final UIAwareIcon COLLAPSE = create("collapse.svg");
    public static final Icon MORE_TABS = loadIcon("moreTabs.svg", 20, 20);
    public static final Icon CLEAR = loadIcon("clear.svg", 12, 12);

    public static final UIAwareIcon MOVE_TOP_LEFT = create("moveToTopLeft.svg");
    public static final UIAwareIcon MOVE_TOP_RIGHT = create("moveToTopRight.svg");
    public static final UIAwareIcon MOVE_RIGHT_TOP = create("moveToRightTop.svg");
    public static final UIAwareIcon MOVE_RIGHT_BOTTOM = create("moveToRightBottom.svg");
    public static final UIAwareIcon MOVE_BOTTOM_RIGHT = create("moveToBottomRight.svg");
    public static final UIAwareIcon MOVE_BOTTOM_LEFT = create("moveToBottomLeft.svg");
    public static final UIAwareIcon MOVE_LEFT_BOTTOM = create("moveToLeftBottom.svg");
    public static final UIAwareIcon MOVE_LEFT_TOP = create("moveToLeftTop.svg");

    @Contract(pure = true)
    private Icons() {
    }

    /*
     * Helper method to create the icons.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static UIAwareIcon create(@NotNull final String name) {
        return new UIAwareIcon(name, "light/" + name);
    }

    @NotNull
    public static Icon loadIcon(@NotNull final String name) {
        return loadIcon(name, 16, 16);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static Icon loadIcon(@NotNull final String name, final int w, final int h) {
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
