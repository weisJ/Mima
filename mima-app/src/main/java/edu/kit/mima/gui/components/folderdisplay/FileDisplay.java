package edu.kit.mima.gui.components.folderdisplay;

import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.Nullable;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Display that shows a file hierarchy that can be navigated.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileDisplay extends JPanel {
    private static final String SEPARATOR = FileName
            .escapeMetaCharacters(System.getProperty("file.separator"));
    private static final int maxLength = 10;
    @Nullable private File file;
    private FilePopupActionHandler handler;

    /**
     * File display panel.
     */
    public FileDisplay() {
        this.handler = f -> {};
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new IconPanel(Icons.FOLDER_ROOT));
    }

    /**
     * Set the popup handler.
     *
     * @param handler handler for directory popups
     */
    public void setHandler(final FilePopupActionHandler handler) {
        this.handler = handler;
    }

    /**
     * Set current displayed file tree.
     *
     * @param file file to display
     */
    public void setFile(@Nullable final File file) {
        if (file == null || file.equals(this.file)) {
            return;
        }
        removeAll();
        this.file = file;
        final String[] directories = file.getPath().split(SEPARATOR);
        final StringBuilder sb = new StringBuilder();
        final int beginIndex = Math.max(directories.length - maxLength, 0);
        for (int i = 0; i < beginIndex; i++) {
            sb.append(directories[i]).append(SEPARATOR);
        }
        if (sb.length() == 0) {
            add(new IconPanel(Icons.FOLDER_ROOT));
        } else {
            add(new FolderDisplay(null, new File(sb.toString()), Icons.FOLDER_ROOT, handler));
        }
        for (int i = beginIndex; i < directories.length; i++) {
            final String s = directories[i];
            sb.append(s).append(SEPARATOR);
            if (!s.contains(":")) {
                add(new IconPanel(Icons.DIVIDER));
                add(new FolderDisplay(s, new File(sb.toString()), handler));
            }
        }
        revalidate();
    }

    /**
     * Focus and click last Element.
     */
    public void focusLast() {
        SwingUtilities.invokeLater(() -> {
            final var c = this.getComponent(getComponentCount() - 1);
            final Point point = c.getLocationOnScreen();
            try {
                final Robot robot = new Robot();
                robot.mouseMove(point.x + c.getWidth() / 2, point.y + c.getHeight() / 2);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } catch (final AWTException ignored) {
            }
        });
    }
}
