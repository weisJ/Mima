package edu.kit.mima.gui.components;

import com.bulenkov.iconloader.util.EmptyIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class IconComponent<T extends Component> extends JComponent {

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    protected final T comp;
    protected final IconPanel iconPanel;
    private final int horizontalGap;
    private final int iconGap;
    private int alignment;

    /**
     * @param comp          the component.
     * @param icon          the icon.
     * @param alignment     the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     * @param horizontalGap the horizontal gap before and after the label.
     * @param iconGap       the gap between the icon and the component.
     */
    public IconComponent(final T comp, final Icon icon, final int alignment,
                         final int horizontalGap, final int iconGap) {
        this.comp = comp;
        this.alignment = alignment;
        this.horizontalGap = horizontalGap;
        this.iconGap = iconGap;
        iconPanel = new IconPanel(icon == null ? new EmptyIcon(0, 0) : icon);
        iconPanel.setOpaque(false);
        setLayout(new TabLayout());
        add(comp);
        add(iconPanel);
    }

    /**
     * Create new Icon Component.
     *
     * @param comp      the component.
     * @param icon      the icon.
     * @param alignment the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     */
    public IconComponent(final T comp, final Icon icon, final int alignment) {
        this(comp, icon, alignment, 5, 5);
    }

    /**
     * Create new Icon Component.
     *
     * @param comp the component.
     * @param icon the icon.
     */
    public IconComponent(final T comp, final Icon icon) {
        this(comp, icon, CENTER, 5, 5);
    }

    /**
     * Get the icon.
     *
     * @return the icon
     */
    @NotNull
    public Icon getIcon() {
        return iconPanel.getIcon();
    }

    /**
     * Set the icon.
     *
     * @param icon the icon. null if no icon should be used.
     */
    public void setIcon(final Icon icon) {
        iconPanel.setIcon(icon == null ? new EmptyIcon(0, 0) : icon);
    }

    /**
     * Get the component.
     *
     * @return the component.
     */
    public T getComponent() {
        return comp;
    }

    @Override
    protected void paintComponent(@NotNull final Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void paint(final Graphics g) {
        getLayout().layoutContainer(this);
        super.paint(g);
    }

    /**
     * Get the alignment.
     *
     * @return one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}.
     */
    public int getHorizontalAlignment() {
        return alignment;
    }

    /**
     * Set the alignment.
     *
     * @param alignment one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}.
     */
    public void setHorizontalAlignment(final int alignment) {
        this.alignment = alignment;
    }

    @Override
    public Dimension getMinimumSize() {
        return getLayout().minimumLayoutSize(this);
    }

    private class TabLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(final String name, final Component comp) {
        }

        @Override
        public void removeLayoutComponent(final Component comp) {
        }

        @NotNull
        @Override
        public Dimension preferredLayoutSize(final Container parent) {
            return minimumLayoutSize(parent);
        }

        @NotNull
        @Override
        public Dimension minimumLayoutSize(final Container parent) {
            var lm = comp.getPreferredSize();
            var im = iconPanel.getMinimumSize();
            int gap = switch (alignment) {
                case LEFT, RIGHT -> horizontalGap;
                case CENTER -> 2 * horizontalGap;
                default -> 0;
            };
            return new Dimension(lm.width + im.width + gap + iconGap,
                                 Math.max(lm.height, im.height) + 1);
        }

        @Override
        public void layoutContainer(@NotNull final Container parent) {
            var b = comp.getPreferredSize();
            var ib = iconPanel.getPreferredSize();
            int x = switch (alignment) {
                case LEFT -> horizontalGap;
                case RIGHT -> parent.getWidth() - b.width - ib.width - horizontalGap;
                default -> parent.getWidth() / 2 - (b.width + ib.width) / 2;
            };
            int y = (parent.getHeight() - b.height) / 2;
            int iy = (parent.getHeight() - ib.height) / 2;
            iconPanel.setBounds(x, iy, ib.width, ib.height);
            comp.setBounds(x + ib.width + iconGap, y, b.width, b.height);
        }
    }
}
