package edu.kit.mima.gui.components.listeners;

import org.jetbrains.annotations.Contract;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class HoverListener implements MouseListener {

    private final JComponent component;
    private boolean hover = false;
    private boolean scheduled = false;

    @Contract(pure = true)
    public HoverListener(final JComponent component) {
        this.component = component;
    }

    public boolean isHover() {
        return hover;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

    }

    @Override
    public void mousePressed(final MouseEvent e) {

    }

    @Override
    public void mouseReleased(final MouseEvent e) {

    }

    private void scheduleRepaint() {
        if (!scheduled) {
            scheduled = true;
            SwingUtilities.invokeLater(() -> {
                component.invalidate();
                component.repaint();
                scheduled = false;
            });
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (!hover) {
            hover = true;
            scheduleRepaint();
        }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        if (hover) {
            hover = false;
            scheduleRepaint();
        }
    }
}
