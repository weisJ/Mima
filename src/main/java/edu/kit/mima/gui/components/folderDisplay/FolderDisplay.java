package edu.kit.mima.gui.components.folderDisplay;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.laf.icons.Icons;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FolderDisplay extends JPanel {

    /**
     * FolderDisplay panel
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param handler action handler for popup
     */
    FolderDisplay(String name, File file, FilePopupActionHandler handler) {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        var tooltip = new DirectoryTooltip(file, handler);
        String title = file.isDirectory() ? name : name.substring(0, name.lastIndexOf('.'));
        var label = new JLabel(title);
        label.setBorder(new EmptyBorder(0, 2, 0, 2));
        addMouseListener(new PopupListener(tooltip, MouseEvent.BUTTON1, true));
        add(new IconPanel(Icons.DIVIDER, Alignment.WEST));
        add(new IconPanel(Icons.forFile(file), Alignment.WEST));
        add(label);
    }
}
