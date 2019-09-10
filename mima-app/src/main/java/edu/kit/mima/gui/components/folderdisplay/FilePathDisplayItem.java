package edu.kit.mima.gui.components.folderdisplay;

import edu.kit.mima.annotations.ContextManager;
import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.components.listeners.FilePopupActionHandler;
import edu.kit.mima.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Display for one specific file/folder.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FilePathDisplayItem extends JPanel {

    @NotNull
    private final File file;
    @NotNull
    private final FilePopupActionHandler handler;

    /**
     * FilePathDisplayItem panel.
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param handler action handler for popup
     */
    /*default*/ FilePathDisplayItem(final String name, @NotNull final File file,
                                    @NotNull final FilePopupActionHandler handler) {
        this(name, file, IconUtil.forFile(file), handler);
    }

    /**
     * FilePathDisplayItem panel.
     *
     * @param name    name of folder/item
     * @param file    file associated with this component
     * @param icon    file icon
     * @param handler action handler for popup
     */
    /*default*/ FilePathDisplayItem(@Nullable final String name,
                                    @NotNull final File file,
                                    @NotNull final Icon icon,
                                    @NotNull final FilePopupActionHandler handler) {
        this.file = file;
        this.handler = handler;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentY(Component.CENTER_ALIGNMENT);
        setOpaque(false);
        ContextManager.createContext(this);
        add(new IconPanel(icon));
        if (name != null && name.length() != 0) {
            final String title = file.isDirectory() ? name : name.substring(0, name.lastIndexOf('.'));
            final JLabel label = new JLabel(title);
            label.setBorder(new EmptyBorder(0, 2, 0, 2));
            add(label);
        }
    }

    @NotNull
    public FilePopupActionHandler getHandler() {
        return handler;
    }

    @NotNull
    public File getFile() {
        return file;
    }
}
