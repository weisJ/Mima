package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Panel for drawing icons.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IconPanel extends JPanel {
    @NotNull private final Icon icon;

    /**
     * Create new Icon panel.
     *
     * @param icon        icon to draw.
     */
    public IconPanel(@NotNull final Icon icon) {
        this.icon = icon;
        var dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        this.setPreferredSize(dim);
        setOpaque(false);
    }

    @Override
    public void paint(final Graphics g) {
        icon.paintIcon(this, g, 0, 0);
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
}
