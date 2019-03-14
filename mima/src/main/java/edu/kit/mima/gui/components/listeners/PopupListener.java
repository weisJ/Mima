package edu.kit.mima.gui.components.listeners;

import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
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
    public PopupListener(JPopupMenu popupMenu, int mouseButton,
                         boolean attachToComponent) {
        this.popupMenu = popupMenu;
        this.mouseButton = mouseButton;
        this.attachToComponent = attachToComponent;
    }

    public PopupListener(JPopupMenu popupMenu) {
        this(popupMenu, -1, false);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    /**
     * Show popup if trigger was pressed
     *
     * @param e mouse event.
     */
    private void maybeShowPopup(MouseEvent e) {
        var c = e.getComponent();
        int xPos = attachToComponent ? 0 : e.getX();
        int yPos = attachToComponent ? c.getHeight() : e.getY();
        if ((mouseButton == -1 && e.isPopupTrigger()) || (e.getButton() == mouseButton)) {
            popupMenu.show(c, xPos, yPos);
        }
    }
}
