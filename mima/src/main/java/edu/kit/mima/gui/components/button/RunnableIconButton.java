package edu.kit.mima.gui.components.button;

import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class RunnableIconButton extends IconButton implements ActionListener {

    private boolean toggled;
    private Icon running;
    private boolean isRunning;

    public RunnableIconButton(Icon inactive, Icon active, Icon running) {
        super(inactive, active);
        this.running = running;
        this.inactive = inactive;
        this.active = active;
        addActionListener(this);
    }

    @Override
    protected Icon currentIcon() {
        return isRunning ? running : isEnabled() ? active : inactive;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toggled = !toggled;
    }

    /**
     * Toggle the icon
     *
     * @param activeIcon whether to use the active icon
     */
    public void toggle(boolean activeIcon) {
        toggled = activeIcon;
    }
}
