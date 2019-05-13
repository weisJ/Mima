package edu.kit.mima.gui.components.folderdisplay;

import com.intellij.openapi.util.io.FileUtil;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.util.FileName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;

/**
 * Display that shows a file hierarchy that can be navigated.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FilePathDisplay extends JPanel {
    private static final String SEPARATOR =
            FileName.escapeMetaCharacters(System.getProperty("file.separator"));
    private int maxLength = 10;
    @Nullable
    private File file;
    private FilePopupActionHandler handler;
    private Component[] dirComps;
    private Component root;
    private int firstVisible;

    /**
     * File display panel.
     */
    public FilePathDisplay() {
        this.handler = f -> {
        };
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        root = add(new IconPanel(Icons.FOLDER_ROOT), 0);
        dirComps = new Component[0];
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
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void setFile(@Nullable final File file) {
        if (file == null || file.toString().isEmpty() || FileUtil.filesEqual(file, this.file)) {
            return;
        }
        removeAll();
        this.file = file;
        final String[] directories = file.getPath().split(SEPARATOR);
        dirComps = new Component[2 * (directories.length - 1)];
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (final String s : directories) {
            sb.append(s).append(SEPARATOR);
            if (!s.contains(":")) {
                dirComps[index++] = add(new IconPanel(Icons.DIVIDER));
                dirComps[index++] = add(new FilePathDisplayItem(s, new File(sb.toString()), handler));
            }
        }
        firstVisible = maxLength;
        setMaximumSize(getMaximumSize());
    }

    private void updateRoot(@NotNull final String[] directories, final int max) {
        if (root != null) {
            remove(root);
        }
        final int beginIndex = Math.min(Math.max(directories.length - max, 0), directories.length - 1);
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < beginIndex; i++) {
            sb.append(directories[i]).append(SEPARATOR);
            dirComps[index++].setVisible(false); // Separator
            dirComps[index++].setVisible(false); // Item
        }
        sb.append(directories[beginIndex]);
        for (int i = beginIndex; i < directories.length - 1; i++) {
            dirComps[index++].setVisible(true); // Separator
            dirComps[index++].setVisible(true); // Item
        }
        if (sb.length() == 0) {
            root = new IconPanel(Icons.FOLDER_ROOT);
        } else {
            root = new FilePathDisplayItem(null, new File(sb.toString()), Icons.FOLDER_ROOT, handler);
        }
        if (max > 0) {
            add(root, 0);
        }
    }

    /**
     * Get the current maximum length for parent directories.
     *
     * @return max length for parent directories.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Set the maximum length for parent directories.
     *
     * @param maxLength maximum amount of parent directories to be displayed.
     */
    public void setMaxLength(final int maxLength) {
        if (this.maxLength != maxLength) {
            this.maxLength = maxLength;
            setFile(file);
        }
    }

    @Override
    public void setMaximumSize(@NotNull final Dimension maximumSize) {
        super.setMaximumSize(maximumSize);
        if (file == null) {
            return;
        }
        int width = root.getPreferredSize().width;
        int index = dirComps.length - 1;
        int length = 0;
        while (width <= maximumSize.width && index >= 0) {
            width += dirComps[index--].getPreferredSize().width;
            width += dirComps[index--].getPreferredSize().width;
            length++;
        }
        length = Math.min(maxLength, length);
        if (firstVisible != length) {
            firstVisible = length;
            updateRoot(file.getPath().split(SEPARATOR), length);
        }
        invalidate();
    }

    /**
     * Focus and click last Element.
     */
    public void focusLast() {
        SwingUtilities.invokeLater(
                () -> {
                    final var c = this.getComponent(getComponentCount() - 1);
                    final Point point = c.getLocationOnScreen();
                    try {
                        final Robot robot = new Robot();
                        robot.mouseMove(point.x + c.getWidth() / 2, point.y + c.getHeight() / 2);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    } catch (@NotNull final AWTException ignored) {
                    }
                });
    }
}
