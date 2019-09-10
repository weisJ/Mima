package edu.kit.mima.gui.components.button;

import edu.kit.mima.gui.components.listeners.HoverListener;
import edu.kit.mima.gui.components.tooltip.TooltipAware;
import edu.kit.mima.gui.components.tooltip.TooltipEventHandler;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Button that is visualized by an {@link Icon} based on state.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IconButton extends JButton implements TooltipAware {
    protected final int border = 3;
    private final Color highlight;
    private final Color highlightClick;
    private final HoverListener hoverListener;
    protected Icon inactive;
    protected Icon active;
    private boolean clicking;
    private boolean locked;
    private boolean nextStatus;
    private boolean tooltipShown;
    private TooltipEventHandler tooltipEventHandler;

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
        this.inactive = inactive;
        this.active = active;
        highlight = new HSLColor(new JPanel().getBackground()).adjustTone(30).getRGB();
        highlightClick = new HSLColor(highlight).adjustTone(20).getRGB();
        setOpaque(false);
        setRolloverEnabled(true);
        setFocusable(false);
        setBorderPainted(false);
        hoverListener = new HoverListener(this);
        addMouseListener(hoverListener);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    clicking = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    clicking = false;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    for (final var l : getActionListeners()) {
                        l.actionPerformed(new ActionEvent(IconButton.this, 0, ""));
                    }
                }
            }
        });
    }

    @Override
    protected void processMouseEvent(final MouseEvent e) {
        if (!tooltipShown) {
            super.processMouseEvent(e);
        } else {
            if (tooltipEventHandler != null) {
                int id = e.getID();
                switch (id) {
                    case MouseEvent.MOUSE_PRESSED -> tooltipEventHandler.mousePressed(e);
                    case MouseEvent.MOUSE_RELEASED -> tooltipEventHandler.mouseReleased(e);
                    case MouseEvent.MOUSE_CLICKED -> tooltipEventHandler.mouseClicked(e);
                    case MouseEvent.MOUSE_EXITED -> tooltipEventHandler.mouseExited(e);
                    case MouseEvent.MOUSE_ENTERED -> tooltipEventHandler.mouseEntered(e);
                }
            }
        }
    }

    protected Icon currentIcon() {
        return isEnabled() ? active : inactive;
    }

    protected boolean isHover() {
        return hoverListener != null && hoverListener.isHover();
    }

    @Override
    protected void paintComponent(@NotNull final Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isEnabled() && isRolloverEnabled() && isHover()) {
            if (clicking) {
                g.setColor(highlightClick);
            } else {
                g.setColor(highlight);
            }
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        }
        currentIcon().paintIcon(this, g, border, border);
    }

    @NotNull
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Math.max(active.getIconWidth(), inactive.getIconWidth()) + 2 * border,
                             Math.max(active.getIconHeight(), inactive.getIconHeight()) + 2 * border);
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @NotNull
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void updateUI() {
    }

    protected void setEnabledDirect(final boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void setEnabled(final boolean b) {
        // Remember state for when button is unlocked again.
        nextStatus = b;
        if (!locked) {
            super.setEnabled(b);
            repaint();
            locked = true;
            // Lock button to prevent spamming
            new Thread(() -> {
                synchronized (this) {
                    try {
                        wait(300);
                        locked = false;
                        setEnabledDirect(nextStatus);
                        repaint();
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
    }

    @Override
    public void setTooltipVisible(final boolean visible, final TooltipEventHandler eventHandler) {
        this.tooltipShown = visible;
        this.tooltipEventHandler = eventHandler;
    }
}
