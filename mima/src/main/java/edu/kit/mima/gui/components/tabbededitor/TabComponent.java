package edu.kit.mima.gui.components.tabbededitor;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component for tabs in {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabComponent extends JPanel {
    @NotNull
    private final JLabel text;

    /**
     * Create new Tab Component.
     *
     * @param title   title of tab.
     * @param icon    icon of tab.
     * @param onClick event handler when closing.
     */
    public TabComponent(final String title,
                        @Nullable final Icon icon,
                        @NotNull final Consumer<TabComponent> onClick) {
        final Icon newicon = icon == null ? new EmptyIcon(0, 0) : icon;
        final JLabel ic = new JLabel(newicon);
        ic.setSize(newicon.getIconWidth(), newicon.getIconHeight());

        text = new JLabel(title);
        text.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        final ButtonTab button = new ButtonTab();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                onClick.accept(TabComponent.this);
            }
        });
        button.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setSize(getWidth() - newicon.getIconWidth(), getHeight());
        p.add(text);
        p.add(button);

        setOpaque(false);
        text.setOpaque(false);
        ic.setOpaque(false);
        button.setOpaque(false);
        p.setOpaque(false);

        add(ic);
        add(p);
    }

    /**
     * Set the title for the tab component.
     *
     * @param title title to use.
     */
    public void setTitle(final String title) {
        text.setText(title);
    }

    private class ButtonTab extends JButton {

        /**
         * Close Button for Tab.
         */
        public ButtonTab() {
            final int size = 13;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Close");

            setUI(new BasicButtonUI());

            setFocusable(false);
            setBorderPainted(false);

            setRolloverEnabled(true);
        }

        @Override
        public void updateUI() {
        }

        @Override
        protected void paintComponent(@NotNull final Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new HSLColor(getForeground()).adjustShade(20).getRGB());
            if (getModel().isRollover()) {
                g2.setColor(new HSLColor(getForeground()).adjustTone(10).getRGB());
            }

            final int delta = 3;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}
