package edu.kit.mima.gui.components.listeners;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        this(popupMenu, mouseButton, attachToComponent, false);
    }


    public PopupListener(final JPopupMenu popupMenu) {
        this(popupMenu, MouseEvent.BUTTON2, false);
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
    private void maybeShowPopup(@NotNull final MouseEvent e) {
        if (popupMenu == null) {
            return;
        }
        if (e.getButton() == mouseButton) {
            var p = calculatePos(e.getPoint(), e.getComponent());
            popupMenu.show(e.getComponent(), p.x, p.y);
        }
    }

    @NotNull
    @Contract("_, _ -> new")
    private Point calculatePos(@NotNull final Point p, @NotNull final Component c) {
        var x = 0;
        var y = 0;
        if (attachToComponent && rightAlign) {
            y = c.getHeight();
            x = c.getWidth() - popupMenu.getPreferredSize().width;
        } else if (attachToComponent) {
            y = c.getHeight();
        } else {
            x = p.x;
        }
        return new Point(x, y);
    }
}
