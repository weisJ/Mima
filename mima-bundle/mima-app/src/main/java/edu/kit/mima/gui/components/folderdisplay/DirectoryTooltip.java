package edu.kit.mima.gui.components.folderdisplay;

import com.weis.darklaf.components.ScrollPopupMenu;
import edu.kit.mima.api.util.FileName;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

/**
 * DefaultTooltipWindow Menu for Displaying directories and files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DirectoryTooltip extends ScrollPopupMenu {

    /**
     * DefaultTooltipWindow menu for displaying directories.
     *
     * @param directory parent directory
     * @param handler   action handler.
     */
    public DirectoryTooltip(@NotNull final File directory, @NotNull final FilePopupActionHandler handler) {
        super(300);
        final File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        Arrays.sort(children, (a,b) -> {
            if ((a.isDirectory() && b.isDirectory()) || (a.isFile() && b.isFile())) {
                return a.getName().compareTo(b.getName());
            } else if (a.isDirectory()) {
                return -1;
            } else {
                return 1;
            }
        });
        for (final var file : children) {
            if (!file.isHidden()) {
                final var menuItem = new JMenuItem(new AbstractAction() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        SwingUtilities.invokeLater(() -> handler.onClick(file));
                    }
                });
                String label = FileName.removeExtension(file, MimaConstants.EXTENSIONS);
                if (label.isEmpty()) {
                    label = file.getName();
                }
                menuItem.setText(label);
                menuItem.setIcon(IconUtil.forFile(file));
                add(menuItem);
            }
        }
    }

    @Override
    public void show(final Component invoker, final int x, final int y) {
        super.show(invoker, x, y);
        SwingUtilities.invokeLater(() -> {
            if (getComponentCount() < 1 || !this.isVisible()) {
                return;
            }
            final var c = this.getComponent(0);
            final Point point = c.getLocationOnScreen();
            try {
                final Robot robot = new Robot();
                robot.mouseMove(point.x + c.getWidth() / 3, point.y + c.getHeight() / 2);
            } catch (@NotNull final AWTException ignored) {
            }
        });
    }
}
