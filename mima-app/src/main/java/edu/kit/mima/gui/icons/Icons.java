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
    public static final UIAwareIcon DEBUG_INACTIVE = create("actions/debug_inactive.svg");
    public static final Icon BREAKPOINT = create("breakpoint.svg");
    public static final Icon STACK_POINTER = create("stack_pointer.svg");

    public static final UIAwareIcon DEBUG = create("actions/debug.svg");
    public static final UIAwareIcon DEBUG_ACTIVE = create("actions/debug_active.svg");
    public static final UIAwareIcon RUN_INACTIVE = create("actions/run_inactive.svg");
    public static final UIAwareIcon RUN = create("actions/run.svg");
    public static final UIAwareIcon RUN_ACTIVE = create("actions/run_active.svg");
    public static final UIAwareIcon PAUSE = create("actions/pause.svg");
    public static final UIAwareIcon PAUSE_INACTIVE = create("actions/pause_inactive.svg");
    public static final UIAwareIcon RESUME = create("actions/resume.svg");
    public static final UIAwareIcon RESUME_INACTIVE = create("actions/resume_inactive.svg");
    public static final UIAwareIcon STOP = create("actions/stop.svg");
    public static final UIAwareIcon STOP_INACTIVE = create("actions/stop_inactive.svg");
    public static final UIAwareIcon REDO = create("actions/redo.svg");
    public static final UIAwareIcon REDO_INACTIVE = create("actions/redo_inactive.svg");
    public static final UIAwareIcon UNDO = create("actions/undo.svg");
    public static final UIAwareIcon UNDO_INACTIVE = create("actions/undo_inactive.svg");

    public static final UIAwareIcon FOLDER = create("files/folder.svg");
    public static final UIAwareIcon FOLDER_ROOT = create("files/folder_root.svg");
    public static final UIAwareIcon MIMA = create("files/class.svg");
    public static final UIAwareIcon MIMA_X = create("files/class.svg");
    public static final UIAwareIcon TEXT_FILE = create("files/text.svg");
    public static final UIAwareIcon UNKNOWN_FILE = create("files/unknown.svg");
    public static final UIAwareIcon GENERAL_FILE = create("files/general.svg");
    public static final UIAwareIcon ASSEMBLY_FILE = create("files/assembly.svg");

    public static final UIAwareIcon MEMORY = create("memory.svg");
    public static final UIAwareIcon CONSOLE = create("console.svg");
    public static final UIAwareIcon TERMINAL = create("terminal.svg");
    public static final UIAwareIcon BUILD_GREY = create("build.svg");

    public static final UIAwareIcon CUT = create("menu/cut.svg");
    public static final UIAwareIcon PASTE = create("menu/paste.svg");
    public static final UIAwareIcon COPY = create("menu/copy.svg");
    public static final UIAwareIcon DELETE = create("menu/delete.svg");


    public static final UIAwareIcon DIVIDER = create("navigation/divider.svg");
    public static final UIAwareIcon SEARCH = create("navigation/search.svg");
    public static final UIAwareIcon SEARCH_WITH_HISTORY = create("navigation/searchWithHistory.svg");
    public static final UIAwareIcon CLOSE = create("navigation/close.svg");
    public static final UIAwareIcon CLOSE_HOVER = create("navigation/closeHovered.svg");
    public static final UIAwareIcon COLLAPSE = create("navigation/collapse.svg");
    public static final UIAwareIcon ADD = create("navigation/add.svg");
    public static final UIAwareIcon MOVE_TOP_LEFT = create("navigation/moveToTopLeft.svg");
    public static final UIAwareIcon MOVE_TOP_RIGHT = create("navigation/moveToTopRight.svg");
    public static final UIAwareIcon MOVE_RIGHT_TOP = create("navigation/moveToRightTop.svg");
    public static final UIAwareIcon MOVE_RIGHT_BOTTOM = create("navigation/moveToRightBottom.svg");
    public static final UIAwareIcon MOVE_BOTTOM_RIGHT = create("navigation/moveToBottomRight.svg");
    public static final UIAwareIcon MOVE_BOTTOM_LEFT = create("navigation/moveToBottomLeft.svg");
    public static final UIAwareIcon MOVE_LEFT_BOTTOM = create("navigation/moveToLeftBottom.svg");
    public static final UIAwareIcon MOVE_LEFT_TOP = create("navigation/moveToLeftTop.svg");
    public static final UIAwareIcon ARROW_UP = create("navigation/arrowUp.svg");
    public static final UIAwareIcon ARROW_LEFT = create("navigation/arrowLeft.svg");
    public static final UIAwareIcon ARROW_RIGHT = create("navigation/arrowRight.svg");
    public static final UIAwareIcon ARROW_DOWN = create("navigation/arrowDown.svg");
    public static final UIAwareIcon PROJECT = create("project.svg");

    private static final Icons instance = new Icons();
    public static final Icon MORE_TABS = loadIcon("dark/navigation/moreTabs.svg", 20, 20);
    public static final Icon CLEAR = loadIcon("dark/navigation/clear.svg", 12, 12);


    @Contract(pure = true)
    private Icons() {
    }

    /*
     * Helper method to create the icons.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static UIAwareIcon create(@NotNull final String name) {
        return new UIAwareIcon("dark/" + name, "light/" + name);
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
        } catch (NullPointerException | IOException e) {
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
        } else if (!name.contains(".")) {
            return UNKNOWN_FILE;
        } else {
            String fileType = null;
            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (@NotNull final IOException ignored) {
            }
            if (fileType != null && fileType.startsWith("text")) {
                return TEXT_FILE;
            } else {
                return GENERAL_FILE;
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
