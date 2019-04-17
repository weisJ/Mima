package edu.kit.mima.gui.components;

import com.bulenkov.iconloader.util.EmptyIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;

/**
 * Label with icon.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class IconLabel extends JPanel {

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    protected final JLabel label;
    protected IconPanel iconPanel;
    private int alignment;
    private int horizontalGap;

    /**
     * Create new Icon label.
     *
     * @param icon          the icon.
     * @param title         the label text.
     * @param alignment     the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     * @param horizontalGap the horizontal gap before and after the label.
     */
    public IconLabel(final Icon icon, final String title, int alignment, int horizontalGap) {
        label = new JLabel(title);
        iconPanel = new IconPanel(icon == null ? new EmptyIcon(0, 0) : icon);

        label.setOpaque(false);
        iconPanel.setOpaque(false);
        this.alignment = alignment;
        this.horizontalGap = horizontalGap;
        setLayout(new TabLayout());
        add(label);
        add(iconPanel);
    }

    /**
     * Create new Icon label.
     *
     * @param icon      the icon.
     * @param title     the label text.
     * @param alignment the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     */
    public IconLabel(final Icon icon, final String title, int alignment) {
        this(icon, title, alignment, 5);
    }

    /**
     * Create new Icon label.
     *
     * @param icon  the icon.
     * @param title the label text.
     */
    public IconLabel(final Icon icon, final String title) {
        this(icon, title, CENTER, 5);
    }

    /**
     * Get the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return iconPanel.getIcon();
    }

    /**
     * Get the title.
     *
     * @return the title.
     */
    public String getTitle() {
        return label.getText();
    }

    /**
     * Set the title.
     *
     * @param title the title
     */
    public void setTitle(final String title) {
        label.setText(title);
    }

    @Override
    public void paint(Graphics g) {
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
    public void setHorizontalAlignment(int alignment) {
        this.alignment = alignment;
    }

    @Override
    public Dimension getMinimumSize() {
        return getLayout().minimumLayoutSize(this);
    }

    private class TabLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            var lm = label.getPreferredSize();
            var im = iconPanel.getPreferredSize();
            return new Dimension(lm.width + im.width + 2 * horizontalGap,
                                 Math.max(lm.height, im.height) + 1);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            var lm = label.getMinimumSize();
            var im = iconPanel.getMinimumSize();
            int gap = switch (alignment) {
                case LEFT, RIGHT -> horizontalGap;
                case CENTER -> 2 * horizontalGap;
                default -> 0;
            };
            return new Dimension(lm.width + im.width + gap,
                                 Math.max(lm.height, im.height) + 1);
        }

        @Override
        public void layoutContainer(@NotNull Container parent) {
            var b = label.getPreferredSize();
            var ib = iconPanel.getPreferredSize();
            int x = switch (alignment) {
                case LEFT -> horizontalGap;
                case RIGHT -> parent.getWidth() - b.width - ib.width - horizontalGap;
                default -> parent.getWidth() / 2 - (b.width + ib.width) / 2;
            };
            int y = parent.getHeight() - b.height - 2;
            int iy = parent.getHeight() - ib.height - 2;
            iconPanel.setBounds(x, iy, ib.width, ib.height);
            label.setBounds(x + ib.width + 1, y, b.width, b.height);
        }
    }
}