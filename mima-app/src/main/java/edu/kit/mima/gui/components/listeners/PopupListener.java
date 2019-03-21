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
    private final int mouseButton;
    private final boolean attachToComponent;
    private final boolean rightAlign;
    private JPopupMenu popupMenu;

    /**
     * Create new PopupListener.
     *
     * @param popupMenu         PopupMenu to display.
     * @param mouseButton       activation button to use.
     * @param attachToComponent whether the menu should be attached to the component.
     * @param rightAlign        whether to align right when attached to component.
     */
    public PopupListener(final JPopupMenu popupMenu, final int mouseButton,
                         final boolean attachToComponent, final boolean rightAlign) {
        this.popupMenu = popupMenu;
        this.rightAlign = rightAlign;
        this.mouseButton = mouseButton;
        this.attachToComponent = attachToComponent;
    }

    public PopupListener(final JPopupMenu popupMenu, final int mouseButton,
                         final boolean attachToComponent) {
        this(popupMenu, -1, false, false);
    }


    public PopupListener(final JPopupMenu popupMenu) {
        this(popupMenu, -1, false);
    }

    /**
     * Set the popup menu.
     *
     * @param menu menu to show.
     */
    public void setPopupMenu(JPopupMenu menu) {
        this.popupMenu = menu;
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
        if (popupMenu == null) {
            return;
        }
        final var c = e.getComponent();
        int xpos = attachToComponent
                ? rightAlign ? c.getWidth() - popupMenu.getPreferredSize().width : 0
                : e.getX();
        int ypos = attachToComponent ? c.getHeight() : e.getY();
        if ((mouseButton == -1 && e.isPopupTrigger()) || (e.getButton() == mouseButton)) {
            popupMenu.show(c, xpos, ypos);
        }
    }
}
