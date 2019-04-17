package edu.kit.mima.gui.components;

import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Panel for drawing icons.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IconPanel extends JComponent {
    @NotNull private final Icon icon;
    private Alignment alignment;

    /**
     * Create new Icon panel.
     *
     * @param icon        icon to draw.
     */
    public IconPanel(@NotNull final Icon icon) {
        this.icon = icon;
        var dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        this.setPreferredSize(dim);
        alignment = Alignment.NORTH;
        setOpaque(false);
    }

    /**
     * Get the icon.
     *
     * @return the icon.
     */
    @NotNull
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setClip(0, 0, icon.getIconWidth(), icon.getIconHeight());
        AffineTransform transform = new AffineTransform();
        transform.rotate(getAngle(), getWidth() / 2, getHeight() / 2);
        g2.transform(transform);
        icon.paintIcon(this, g2, 0, 0);
    }

    @NotNull
    @Override
    public Color getBackground() {
        return new Color(0, 0, 0, 0);
    }

    @Contract(pure = true)
    private double getAngle() {
        return Math.toRadians(switch (alignment) {
            case NORTH, CENTER -> 0.0;
            case SOUTH -> 180.0;
            case EAST -> 90.0;
            case WEST -> 270.0;
            case NORTH_EAST -> 45.0;
            case NORTH_WEST -> 315.0;
            case SOUTH_EAST -> 135.0;
            case SOUTH_WEST -> 225.0;
        });
    }

    @NotNull
    @Override
    public Dimension getPreferredSize() {
        return getMaximumSize();
    }

    public Alignment getAlignment() {
        return alignment;
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }

    /**
     * Set the alignment.
     *
     * @param alignment the alignment.
     */
    public void setAlignment(@Nullable Alignment alignment) {
        this.alignment = alignment == null ? Alignment.NORTH : alignment;
    }
}
