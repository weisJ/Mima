package edu.kit.mima.gui.components.tabbedEditor;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabComponent extends JPanel {
    private final JLabel text;

    public TabComponent(final String title, Icon icon, Consumer<TabComponent> onClick) {
        Icon icone = icon == null ? new EmptyIcon(0, 0) : icon;
        JLabel ic = new JLabel(icone);
        ic.setSize(icone.getIconWidth(), icone.getIconHeight());

        text = new JLabel(title);
        text.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        ButtonTab button = new ButtonTab();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.accept(TabComponent.this);
            }
        });
        button.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setSize(getWidth() - icone.getIconWidth(), getHeight());
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

    public void setTitle(String title) {
        text.setText(title);
    }

    private class ButtonTab extends JButton {

        /**
         * Close Button for Tab
         */
        public ButtonTab() {
            int size = 13;
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
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new HSLColor(getForeground()).adjustShade(20).getRGB());
            if (getModel().isRollover()) {
                g2.setColor(new HSLColor(getForeground()).adjustTone(10).getRGB());
            }

            int delta = 3;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}
