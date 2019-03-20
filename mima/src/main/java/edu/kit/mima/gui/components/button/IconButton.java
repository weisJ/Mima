package edu.kit.mima.gui.components.button;

import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Button that is visualized by an {@link Icon} based on state.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IconButton extends JButton {
    protected final int border = 3;
    private final Color highlight;
    private final Color highlightClick;
    protected Icon inactive;
    protected Icon active;
    private boolean hover;
    private boolean clicking;
    private boolean locked;
    private boolean nextStatus;

    /**
     * Create Button with always the same icon.
     *
     * @param icon icon to show.
     */
    public IconButton(@NotNull final Icon icon) {
        this(icon, icon);
    }

    /**
     * Create Button with inactive and active icon.
     *
     * @param inactive inactive icon.
     * @param active   active icon.
     */
    public IconButton(@NotNull final Icon inactive, @NotNull final Icon active) {
        super();
        this.inactive = inactive;
        this.active = active;
        highlight = new HSLColor(new JPanel().getBackground()).adjustTone(30).getRGB();
        highlightClick = new HSLColor(highlight).adjustTone(20).getRGB();
        setOpaque(false);
        setRolloverEnabled(true);
        setFocusable(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(
                Math.max(active.getIconWidth(), inactive.getIconWidth()) + 2 * border,
                Math.max(active.getIconHeight(), inactive.getIconHeight()) + 2 * border
        ));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                clicking = true;
                repaint();
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                clicking = false;
                repaint();
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                for (final var l : getActionListeners()) {
                    l.actionPerformed(new ActionEvent(IconButton.this, 0, ""));
                }
            }
        });
    }

    protected Icon currentIcon() {
        return isEnabled() ? active : inactive;
    }

    @Override
    protected void paintComponent(@NotNull final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isEnabled() && hover) {
            if (clicking) {
                g2.setColor(highlightClick);
            } else {
                g2.setColor(highlight);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        }
        currentIcon().paintIcon(this, g2, border, border);
        g2.dispose();
    }

    @Override
    public void updateUI() {
    }

    @Override
    public void setEnabled(final boolean b) {
        //Remember state for when button is unlocked again.
        nextStatus = b;
        if (!locked) {
            super.setEnabled(b);
            repaint();
            locked = true;
            //Lock button to prevent spamming
            new Thread(() -> {
                synchronized (this) {
                    try {
                        wait(300);
                        locked = false;
                        super.setEnabled(nextStatus);
                        repaint();
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
    }
}
