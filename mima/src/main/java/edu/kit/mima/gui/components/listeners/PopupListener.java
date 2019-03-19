package edu.kit.mima.gui.components.listeners;

import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;

/**
 * Listener for Popups.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class PopupListener extends MouseAdapter {
    private final JPopupMenu popupMenu;
    private final int mouseButton;
    private final boolean attachToComponent;

    /**
     * Create new PopupListener.
     *
     * @param popupMenu         PopupMenu to display.
     * @param mouseButton       activation button to use.
     * @param attachToComponent whether the menu should be attached to the component.
     */
    public PopupListener(final JPopupMenu popupMenu, final int mouseButton,
                         final boolean attachToComponent) {
        this.popupMenu = popupMenu;
        this.mouseButton = mouseButton;
        this.attachToComponent = attachToComponent;
    }

    public PopupListener(final JPopupMenu popupMenu) {
        this(popupMenu, -1, false);
    }

    @Override
    public void mouseReleased(@NotNull final MouseEvent e) {
        maybeShowPopup(e);
    }

    /**
     * Show popup if trigger was pressed.
     *
     * @param e mouse event.
     */
    private void maybeShowPopup(final MouseEvent e) {
        final var c = e.getComponent();
        final int xPos = attachToComponent ? 0 : e.getX();
        final int yPos = attachToComponent ? c.getHeight() : e.getY();
        if ((mouseButton == -1 && e.isPopupTrigger()) || (e.getButton() == mouseButton)) {
            popupMenu.show(c, xPos, yPos);
        }
    }
}
