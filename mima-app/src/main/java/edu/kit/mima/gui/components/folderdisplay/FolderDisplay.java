package edu.kit.mima.gui.components.folderdisplay;

import edu.kit.mima.gui.components.Alignment;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.laf.icons.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Display for one specific file/folder.
 *
 * @author Jannis Weis
 * @since 2018
 */
/*default*/ class FolderDisplay extends JPanel {

    /**
     * FolderDisplay panel.
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param handler action handler for popup
     */
    /*default*/ FolderDisplay(final String name,
                              @NotNull final File file,
                              @NotNull final FilePopupActionHandler handler) {
        this(name, file, Icons.forFile(file), handler);
    }

    /**
     * FolderDisplay panel.
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param icon    file icon
     * @param handler action handler for popup
     */
    /*default*/ FolderDisplay(@Nullable final String name,
                              @NotNull final File file,
                              @NotNull final Icon icon,
                              @NotNull final FilePopupActionHandler handler) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        final var tooltip = new DirectoryTooltip(file, handler);
        addMouseListener(new PopupListener(tooltip, MouseEvent.BUTTON1, true));
        add(new IconPanel(icon, Alignment.NORTH_WEST));
        if (name != null && name.length() != 0) {
            final String title = file.isDirectory() ? name
                    : name.substring(0, name.lastIndexOf('.'));
            final JLabel label = new JLabel(title);
            label.setBorder(new EmptyBorder(0, 2, 0, 2));
            add(label);
        }
    }

}