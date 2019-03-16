package edu.kit.mima.gui.laf.icons;

import edu.kit.mima.preferences.MimaConstants;
import org.apache.batik.transcoder.TranscoderException;
import org.jdesktop.swingx.icon.EmptyIcon;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Icons {
    private static final Icons instance = new Icons();
    public static final Icon BREAKPOINT = loadIcon("breakpoint.svg", 12, 12);

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

    public static final Icon REDO = loadIcon("redo.svg");
    public static final Icon REDO_INACTIVE = loadIcon("redo_inactive.svg");
    public static final Icon UNDO = loadIcon("undo.svg");
    public static final Icon UNDO_INACTIVE = loadIcon("undo_inactive.svg");

    private Icons() {
    }

    private static Icon loadIcon(String name) {
        return loadIcon(name, 16, 16);
    }

    private static Icon loadIcon(String name, int w, int h) {
        try {
            return new SVGIcon(Objects.requireNonNull(instance.getClass()
                    .getResource(name)).toURI().toString(), w, h);
        } catch (TranscoderException | URISyntaxException e) {
            e.printStackTrace();
        }
        return new EmptyIcon();
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
            String fileType = null;
            try {
                fileType = Files.probeContentType(file.toPath());
            } catch (IOException ignored) { }
            if (fileType != null && fileType.startsWith("text")) {
                return TEXT_FILE;
            } else {
                return UNKNOWN_FILE;
            }
        }
    }

    public static Icon foFile(String name) {
        return forFile(new File(name));
    }
}
