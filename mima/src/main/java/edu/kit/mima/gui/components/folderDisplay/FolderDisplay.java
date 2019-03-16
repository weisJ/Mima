package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.laf.icons.Icons;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
/*default*/ class FolderDisplay extends JPanel {

    /**
     * FolderDisplay panel
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param handler action handler for popup
     */
    /* default*/ FolderDisplay(String name, File file, FilePopupActionHandler handler) {
        this(name, file, Icons.forFile(file), handler);
    }

    /**
     * FolderDisplay panel
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param icon    file icon
     * @param handler action handler for popup
     */
    /* default*/ FolderDisplay(String name, File file, Icon icon, FilePopupActionHandler handler) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        var tooltip = new DirectoryTooltip(file, handler);
        addMouseListener(new PopupListener(tooltip, MouseEvent.BUTTON1, true));
        add(new IconPanel(icon, Alignment.NORTH_WEST));
        if (name != null && name.length() != 0) {
            String title = file.isDirectory() ? name : name.substring(0, name.lastIndexOf('.'));
            JLabel label = new JLabel(title);
            label.setBorder(new EmptyBorder(0, 2, 0, 2));
            add(label);
        }
    }

}
