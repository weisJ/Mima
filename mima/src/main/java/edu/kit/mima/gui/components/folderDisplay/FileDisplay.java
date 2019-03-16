package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.util.FileName;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileDisplay extends JPanel {
    private static final String SEPARATOR = FileName.escapeMetaCharacters(System.getProperty("file.separator"));
    private static final int maxLength = 10;
    private File file;
    private FilePopupActionHandler handler;

    /**
     * File display panel
     */
    public FileDisplay() {
        this.handler = f -> {};
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new IconPanel(Icons.FOLDER_ROOT, Alignment.CENTER));
    }

    /**
     * Set the popup handler
     *
     * @param handler handler for directory popups
     */
    public void setHandler(FilePopupActionHandler handler) {
        this.handler = handler;
    }

    /**
     * Set current displayed file tree
     *
     * @param file file to display
     */
    public void setFile(File file) {
        if (file == null || file.equals(this.file)) {
            return;
        }
        removeAll();
        this.file = file;
        String[] directories = file.getPath().split(SEPARATOR);
        StringBuilder sb = new StringBuilder();
        int beginIndex = Math.max(directories.length - maxLength, 0);
        for (int i = 0; i < beginIndex; i++) {
            sb.append(directories[i]).append(SEPARATOR);
        }
        if (sb.length() == 0) {
            add(new IconPanel(Icons.FOLDER_ROOT, Alignment.NORTH_WEST));
        } else {
            add(new FolderDisplay(null, new File(sb.toString()), Icons.FOLDER_ROOT, handler));
        }
        for (int i = beginIndex; i < directories.length; i++) {
            String s = directories[i];
            sb.append(s).append(SEPARATOR);
            if (!s.contains(":")) {
                add(new IconPanel(Icons.DIVIDER, Alignment.NORTH_WEST));
                add(new FolderDisplay(s, new File(sb.toString()), handler));
            }
        }
        revalidate();
    }

    /**
     * Focus and click last Element
     */
    public void focusLast() {
        SwingUtilities.invokeLater(() -> {
            var c = this.getComponent(getComponentCount() - 1);
            Point point = c.getLocationOnScreen();
            try {
                Robot robot = new Robot();
                robot.mouseMove(point.x + c.getWidth() / 2, point.y + c.getHeight() / 2);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        });
    }
}
