package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.popupMenu.ScrollPopupMenu;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.util.FileName;
import edu.kit.mima.gui.util.HSLColor;
import edu.kit.mima.preferences.MimaConstants;

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
    public DirectoryTooltip(File directory, FilePopupActionHandler handler) {
        super(300);
        var children = directory.listFiles();
        if (children == null) {
            return;
        }
        setBorder(new LineBorder(new HSLColor(getBackground()).adjustTone(60).getRGB(), 1));
        for (var file : children) {
            if (!file.isHidden()) {
                var menuItem = new JMenuItem(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
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
    public void show(Component invoker, int x, int y) {
        super.show(invoker, x, y);
        SwingUtilities.invokeLater(() -> {
            if (getComponentCount() < 1) return;
            var c = this.getComponent(0);
            Point point = c.getLocationOnScreen();
            try {
                Robot robot = new Robot();
                robot.mouseMove(point.x + c.getWidth() / 3, point.y + c.getHeight() / 2);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        });
    }
}
