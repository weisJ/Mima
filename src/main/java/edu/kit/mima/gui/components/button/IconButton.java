package edu.kit.mima.gui.components.button;

import edu.kit.mima.gui.util.HSLColor;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class IconButton extends JButton {
    protected final int border = 3;
    protected Icon inactive;
    protected Icon active;
    private Color highlight;
    private Color highlightClick;

    private boolean hover;
    private boolean clicking;
    private boolean visible;
    private boolean locked;
    private boolean nextStatus;

    public IconButton(Icon icon) {
        this(icon, icon);
    }

    public IconButton(Icon inactive, Icon active) {
        super();
        this.inactive = inactive;
        this.active = active;
        visible = true;
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
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                clicking = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                clicking = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!visible) {
                    return;
                }
                for (var l : getActionListeners()) {
                    l.actionPerformed(new ActionEvent(IconButton.this, 0, ""));
                }
            }
        });
    }

    protected boolean useAlternative() {
        return isEnabled();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!visible) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isEnabled() && hover) {
            if (clicking) {
                g2.setColor(highlightClick);
            } else {
                g2.setColor(highlight);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        }
        if (useAlternative()) {
            active.paintIcon(this, g2, border, border);
        } else {
            inactive.paintIcon(this, g2, border, border);
        }
        g2.dispose();
    }

    @Override
    public void updateUI() {
    }

    /**
     * Set whether to show the button
     *
     * @param active if true button gets displayed
     */
    public void showButton(boolean active) {
        this.visible = active;
        repaint();
    }

    @Override
    public void setEnabled(boolean b) {
        //Remember state for when button is unlocked again.
        nextStatus = b;
        if (!locked) {
            super.setEnabled(b);
            locked = true;
            //Lock button to prevent spamming
            new Thread(() -> {
                synchronized (this) {
                    try {
                        wait(300);
                        locked = false;
                        super.setEnabled(nextStatus);
                    } catch (InterruptedException ignored) { }
                }
            }).start();
        }
    }
}
