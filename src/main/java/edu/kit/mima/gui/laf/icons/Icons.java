package edu.kit.mima.gui.laf.icons;

import edu.kit.mima.preferences.MimaConstants;
import org.apache.batik.transcoder.TranscoderException;

import javax.swing.Icon;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Icons {
    private static final Icons instance = new Icons();
    public static Icon BREAKPOINT = loadIcon("breakpoint.svg");
    public static Icon DEBUG = loadIcon("debug.svg");
    public static Icon DEBUG_ACTIVE = loadIcon("debug_active.svg");
    public static Icon DIVIDER = loadIcon("divider.svg");
    public static Icon FOLDER = loadIcon("folder.svg");
    public static Icon FOLDER_ROOT = loadIcon("folder_root.svg");
    public static Icon MIMA = loadIcon("class.svg");
    public static Icon MIMA_X = loadIcon("class.svg");
    public static Icon FILE = loadIcon("class.svg");
    public static Icon PAUSE = loadIcon("pause.svg");
    public static Icon PAUSE_INACTIVE = loadIcon("pause_inactive.svg");
    public static Icon REDO = loadIcon("redo.svg");
    public static Icon REDO_INACTIVE = loadIcon("redo_inactive.svg");
    public static Icon UNDO = loadIcon("undo.svg");
    public static Icon UNDO_INACTIVE = loadIcon("undo_inactive.svg");
    public static Icon RESUME = loadIcon("resume.svg");
    public static Icon RUN = loadIcon("run.svg");
    public static Icon RUN_ACTIVE = loadIcon("run_active.svg");
    public static Icon STOP = loadIcon("stop.svg");
    public static Icon STOP_INACTIVE = loadIcon("stop_inactive.svg");

    private Icons() {
    }

    private static Icon loadIcon(String name) {
        try {
            return new SVGIcon(Objects.requireNonNull(instance.getClass()
                    .getResource(name)).toURI().toString());
        } catch (TranscoderException | URISyntaxException e) {
            e.printStackTrace();
            System.out.println(name);
        }
        return null;
    }

    public static void loadIcons() {
        System.out.println("Loaded Icons");
    }

    public static Icon forFile(File file) {
        String name = file.getName();
        if (file.isDirectory()) {
            return FOLDER;
        } else if (name.endsWith("." + MimaConstants.MIMA_EXTENSION)) {
            return MIMA;
        } else if (name.endsWith("." + MimaConstants.MIMA_X_EXTENSION)) {
            return MIMA_X;
        } else {
            return FILE;
        }
    }
}
