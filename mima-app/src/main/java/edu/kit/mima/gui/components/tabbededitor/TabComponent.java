package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.listeners.MouseClickListener;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;

/**
 * Component for tabs in {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabComponent extends JPanel {

    @NotNull
    private final IconLabel iconLabel;

    /**
     * Create new Tab Component.
     *
     * @param title   title of tab.
     * @param icon the icon
     * @param onClick event handler when closing.
     */
    public TabComponent(final String title,
                        @Nullable final Icon icon,
                        @NotNull final Consumer<TabComponent> onClick) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        iconLabel = new IconLabel(icon, title, IconLabel.LEFT);
        iconLabel.setOpaque(false);
        add(iconLabel);
        add(Box.createGlue());

        final ButtonTab button = new ButtonTab();
        button.addMouseListener((MouseClickListener) e -> onClick.accept(TabComponent.this));
        add(button);
        setBorder(new EmptyBorder(3, 0, 2, 5));
        setOpaque(false);
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    public String getTitle() {
        return iconLabel.getTitle();
    }

    /**
     * Set the title for the tab component.
     *
     * @param title title to use.
     */
    public void setTitle(final String title) {
        iconLabel.setTitle(title);
    }

    /**
     * Get the icon.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return iconLabel.getIcon();
    }

    private class ButtonTab extends JButton {

        /**
         * Close Button for Tab.
         */
        private ButtonTab() {
            final int size = 13;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Close");

            setUI(new BasicButtonUI());

            setFocusable(false);
            setBorderPainted(false);

            setRolloverEnabled(true);
            setOpaque(false);
        }

        @Override
        public void updateUI() {
        }

        @Override
        protected void paintComponent(@NotNull final Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                Icons.CLOSE.paintIcon(this, g, 0, 0);
            } else {
                Icons.CLOSE_HOVER.paintIcon(this, g, 0, 0);
            }
            g2.dispose();
        }
    }
}
