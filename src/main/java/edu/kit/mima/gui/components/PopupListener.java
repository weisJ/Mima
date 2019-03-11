package edu.kit.mima.gui.components;

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

    public PopupListener(JPopupMenu popupMenu, int mouseButton) {
        this.popupMenu = popupMenu;
        this.mouseButton = mouseButton;
    }

    public PopupListener(JPopupMenu popupMenu) {
        this(popupMenu, -1);
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (mouseButton == -1) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        } else {
            if (e.getButton() == mouseButton) {
                popupMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
}
