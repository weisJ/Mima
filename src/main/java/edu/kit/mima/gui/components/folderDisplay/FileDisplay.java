package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.util.FileName;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
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
    private File file;
    private FilePopupActionHandler handler;

    /**
     * File display panel
     */
    public FileDisplay() {
        this.handler = f -> {};
        setBorder(new EmptyBorder(0, 5, 0, 5));
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
        add(new IconPanel(Icons.FOLDER_ROOT, Alignment.WEST));
        this.file = file;
        String[] directories = file.getPath().split(SEPARATOR);
        StringBuilder sb = new StringBuilder();
        for (String s : directories) {
            sb.append(s).append(SEPARATOR);
            if (!s.contains(":")) {
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
