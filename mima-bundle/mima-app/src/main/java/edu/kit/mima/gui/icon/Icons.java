package edu.kit.mima.gui.icon;

import com.weis.darklaf.icons.IconLoader;
import com.weis.darklaf.icons.UIAwareIcon;
import org.jetbrains.annotations.Contract;

import javax.swing.*;

/**
 * Icons class for the App. Contains all used icons.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Icons {
    public static final UIAwareIcon DEBUG_INACTIVE = create2("actions/debug_inactive.svg");
    public static final Icon BREAKPOINT = create2("breakpoint.svg");
    public static final Icon STACK_POINTER = create2("stack_pointer.svg");

    public static final UIAwareIcon DEBUG = create2("actions/debug.svg");
    public static final UIAwareIcon DEBUG_ACTIVE = create2("actions/debug_active.svg");
    public static final UIAwareIcon RUN_INACTIVE = create2("actions/run_inactive.svg");
    public static final UIAwareIcon RUN = create2("actions/run.svg");
    public static final UIAwareIcon RUN_ACTIVE = create2("actions/run_active.svg");
    public static final UIAwareIcon PAUSE = create2("actions/pause.svg");
    public static final UIAwareIcon PAUSE_INACTIVE = create2("actions/pause_inactive.svg");
    public static final UIAwareIcon RESUME = create2("actions/resume.svg");
    public static final UIAwareIcon RESUME_INACTIVE = create2("actions/resume_inactive.svg");
    public static final UIAwareIcon STOP = create2("actions/stop.svg");
    public static final UIAwareIcon STOP_INACTIVE = create2("actions/stop_inactive.svg");
    public static final UIAwareIcon REDO = create2("actions/redo.svg");
    public static final UIAwareIcon REDO_INACTIVE = create2("actions/redo_inactive.svg");
    public static final UIAwareIcon UNDO = create2("actions/undo.svg");
    public static final UIAwareIcon UNDO_INACTIVE = create2("actions/undo_inactive.svg");

    public static final UIAwareIcon FOLDER = create("files/folder.svg");
    public static final UIAwareIcon FOLDER_ROOT = create2("files/folder_root.svg");
    public static final UIAwareIcon MIMA = create2("files/class.svg");
    public static final UIAwareIcon MIMA_X = create2("files/class.svg");
    public static final UIAwareIcon TEXT_FILE = create("files/text.svg");
    public static final UIAwareIcon UNKNOWN_FILE = create("files/unknown.svg");
    public static final UIAwareIcon GENERAL_FILE = create2("files/general.svg");
    public static final UIAwareIcon ASSEMBLY_FILE = create2("files/assembly.svg");

    public static final UIAwareIcon MEMORY = create2("memory.svg");
    public static final UIAwareIcon CONSOLE = create2("console.svg");
    public static final UIAwareIcon TERMINAL = create2("terminal.svg");
    public static final UIAwareIcon BUILD_GREY = create2("build.svg");

    public static final UIAwareIcon CUT = create("menu/cut.svg");
    public static final UIAwareIcon PASTE = create("menu/paste.svg");
    public static final UIAwareIcon COPY = create("menu/copy.svg");
    public static final UIAwareIcon DELETE = create("menu/delete.svg");

    public static final UIAwareIcon DIVIDER = create("navigation/divider.svg");
    public static final UIAwareIcon CLOSE = create("navigation/close.svg");
    public static final UIAwareIcon CLOSE_HOVER = create("navigation/closeHovered.svg");
    public static final UIAwareIcon COLLAPSE = create("navigation/collapse.svg");
    public static final UIAwareIcon ADD = create("navigation/add.svg");
    public static final UIAwareIcon MOVE_TOP_LEFT = create2("navigation/moveToTopLeft.svg");
    public static final UIAwareIcon MOVE_TOP_RIGHT = create2("navigation/moveToTopRight.svg");
    public static final UIAwareIcon MOVE_RIGHT_TOP = create2("navigation/moveToRightTop.svg");
    public static final UIAwareIcon MOVE_RIGHT_BOTTOM = create2("navigation/moveToRightBottom.svg");
    public static final UIAwareIcon MOVE_BOTTOM_RIGHT = create2("navigation/moveToBottomRight.svg");
    public static final UIAwareIcon MOVE_BOTTOM_LEFT = create2("navigation/moveToBottomLeft.svg");
    public static final UIAwareIcon MOVE_LEFT_BOTTOM = create2("navigation/moveToLeftBottom.svg");
    public static final UIAwareIcon MOVE_LEFT_TOP = create2("navigation/moveToLeftTop.svg");
    public static final UIAwareIcon PROJECT = create2("project.svg");

    public static final Icon MORE_TABS = IconLoader.get().loadIcon("dark/navigation/moreTabs.svg", 20, 20);

    private static UIAwareIcon create(final String path) {
        return IconLoader.get().getUIAwareIcon(path, 16,16);
    }

    private static UIAwareIcon create2(final String path) {
        return IconLoader.get(Icons.class).getUIAwareIcon(path, 16,16);
    }

    @Contract(pure = true)
    private Icons() {
    }
}
