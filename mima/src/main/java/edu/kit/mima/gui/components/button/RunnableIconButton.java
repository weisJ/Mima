package edu.kit.mima.gui.components.button;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;

/**
 * Extension of {@link IconButton} that has a different icon based on a running state.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class RunnableIconButton extends IconButton implements ActionListener {

    private boolean toggled;
    private Icon running;
    private boolean isRunning;

    /**
     * Create RunnableIconButton.
     *
     * @param inactive inactive icon
     * @param active   active icon.
     * @param running  running button.
     */
    public RunnableIconButton(@NotNull final Icon inactive,
                              @NotNull final Icon active,
                              @NotNull final Icon running) {
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

    public void setRunning(final boolean running) {
        isRunning = running;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        toggled = !toggled;
    }

    /**
     * Toggle the icon.
     *
     * @param activeIcon whether to use the active icon
     */
    public void toggle(final boolean activeIcon) {
        toggled = activeIcon;
    }
}
