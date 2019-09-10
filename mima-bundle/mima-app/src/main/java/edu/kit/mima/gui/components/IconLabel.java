package edu.kit.mima.gui.components;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Label with icon.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class IconLabel extends IconComponent<JLabel> {

    /**
     * Create new Icon label.
     *
     * @param icon          the icon.
     * @param title         the label text.
     * @param alignment     the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     * @param horizontalGap the horizontal gap before and after the label.
     * @param iconGap       the gap between the icon and the component.
     */
    public IconLabel(@Nullable final Icon icon, final String title, final int alignment,
                     final int horizontalGap, final int iconGap) {
        super(new JLabel(title), icon, alignment, horizontalGap, iconGap);
        getComponent().setOpaque(false);
    }

    /**
     * Create new Icon label.
     *
     * @param icon      the icon.
     * @param title     the label text.
     * @param alignment the alignment. one of {@link #LEFT},{@link #CENTER} or {@link #RIGHT}
     */
    public IconLabel(final Icon icon, final String title, final int alignment) {
        this(icon, title, alignment, 5, 5);
    }

    /**
     * Create new Icon label.
     *
     * @param icon  the icon.
     * @param title the label text.
     */
    public IconLabel(final Icon icon, final String title) {
        this(icon, title, CENTER, 5, 5);
    }


    /**
     * Get the title.
     *
     * @return the title.
     */
    public String getTitle() {
        return getComponent().getText();
    }

    /**
     * Set the title.
     *
     * @param title the title
     */
    public void setTitle(final String title) {
        getComponent().setText(title);
    }
}