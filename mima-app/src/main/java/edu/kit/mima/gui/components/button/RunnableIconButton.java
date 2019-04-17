package edu.kit.mima.gui.components.button;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * Extension of {@link IconButton} that has a different icon based on a running state.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class RunnableIconButton extends IconButton {

    @NotNull
    private final Icon running;
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
    }

    @NotNull
    @Override
    protected Icon currentIcon() {
        return isRunning ? running : isEnabled() ? active : inactive;
    }

    public void setRunning(final boolean running) {
        isRunning = running;
    }
}
