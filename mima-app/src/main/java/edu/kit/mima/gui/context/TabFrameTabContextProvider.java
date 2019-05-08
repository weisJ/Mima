package edu.kit.mima.gui.context;

import edu.kit.mima.annotations.Context;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.components.tabframe.TabFrameTabComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Provider class for the {@link TabFrameTabComponent} context menu.
 *
 * @author Jannis Weis
 * @since 2019
 */
@Context(provides = TabFrameTabComponent.class)
public class TabFrameTabContextProvider {

    /**
     * Create and register a context menu for the given {@link TabFrameTabComponent}.
     *
     * @param target the target component.
     */
    @ReflectionCall
    public static void createContextMenu(@NotNull final TabFrameTabComponent target) {
        var menu = new JPopupMenu();
        var remove = new JMenuItem();
        remove.setAction(
                new AbstractAction("Remove from Sidebar") {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        target.removeFromParent();
                    }
                });
        menu.add(remove);
        var hide = new JMenuItem();
        hide.setAction(
                new AbstractAction("Hide") {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        target.setPopupVisible(false);
                    }
                });
        menu.addSeparator();
        menu.add(hide);
        var listener = new PopupListener(menu);
        listener.setUseAbsolutePos(true);
        target.addMouseListener(listener);
    }
}
