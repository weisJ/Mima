package edu.kit.mima.gui.components.folderdisplay;

import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

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
    /*default*/ FolderDisplay(
            final String name, @NotNull final File file, @NotNull final FilePopupActionHandler handler) {
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
    /*default*/ FolderDisplay(
            @Nullable final String name,
            @NotNull final File file,
            @NotNull final Icon icon,
            @NotNull final FilePopupActionHandler handler) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentY(Component.CENTER_ALIGNMENT);
        setOpaque(false);
        final var tooltip = new DirectoryTooltip(file, handler);
        addMouseListener(new PopupListener(tooltip, MouseEvent.BUTTON1, true));
        add(new IconPanel(icon));
        if (name != null && name.length() != 0) {
            final String title = file.isDirectory() ? name : name.substring(0, name.lastIndexOf('.'));
            final JLabel label = new JLabel(title);
            label.setBorder(new EmptyBorder(0, 2, 0, 2));
            add(label);
        }
    }
}
