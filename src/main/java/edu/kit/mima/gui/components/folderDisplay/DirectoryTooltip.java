package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.util.FileName;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class DirectoryTooltip extends JPopupMenu {

    /**
     * Tooltip menu for displaying directories.
     *
     * @param directory parent directory
     * @param handler   action handler.
     */
    public DirectoryTooltip(File directory, FilePopupActionHandler handler) {
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
                String label = FileName.removeExtension(file);
                if (label.isEmpty()) {
                    label = file.getName();
                }
                menuItem.setText(label);
                menuItem.setIcon(Icons.forFile(file));
                add(menuItem);
            }
        }
    }
}
