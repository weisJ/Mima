package edu.kit.mima.gui.components.folderdisplay;

import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.popupmenu.ScrollPopupMenu;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.util.FileName;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Tooltip Menu for Displaying directories and files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DirectoryTooltip extends ScrollPopupMenu {

    /**
     * Tooltip menu for displaying directories.
     *
     * @param directory parent directory
     * @param handler   action handler.
     */
    public DirectoryTooltip(@NotNull final File directory,
                            @NotNull final FilePopupActionHandler handler) {
        super(300);
        final var children = directory.listFiles();
        if (children == null) {
            return;
        }
        setBorder(new LineBorder(new HSLColor(getBackground()).adjustTone(60).getRGB(), 1));
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
                menuItem.setIcon(Icons.forFile(file));
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
            } catch (final AWTException ignored) {
            }
        });
    }
}
