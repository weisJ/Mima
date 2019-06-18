package edu.kit.mima.gui.context;

import edu.kit.mima.annotations.Context;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.components.tabframe.TabFrameTabComponent;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.icons.UIAwareIcon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider class for the {@link TabFrameTabComponent} context menu.
 *
 * @author Jannis Weis
 * @since 2019
 */
@Context(provides = TabFrameTabComponent.class)
public final class TabFrameTabContextProvider {

    /**
     * Create and register a context menu for the given {@link TabFrameTabComponent}.
     *
     * @param target the target component.
     */
    @ReflectionCall
    public static void createContextMenu(@NotNull final TabFrameTabComponent target) {
        var menu = new JPopupMenu();
        var remove = new JMenuItem();
        remove.setAction(new AbstractAction("Remove from Sidebar") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                target.removeFromParent();
            }
        });
        menu.add(remove);
        var hide = new JMenuItem();
        hide.setAction(new AbstractAction("Hide") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                target.setPopupVisible(false);
            }
        });
        menu.addSeparator();
        var moveToMenu = new JMenu("Move to");
        Map<Alignment, JMenuItem> others = new HashMap<>();

        Alignment a = Alignment.NORTH;
        do {
            var item = moveToEntry(a, target, others);
            others.put(a, item);
            moveToMenu.add(item);
            a = a.clockwise();
        } while (a != Alignment.NORTH);


        menu.add(moveToMenu);
        menu.addSeparator();
        menu.add(hide);
        var listener = new PopupListener(menu);
        listener.setUseAbsolutePos(true);
        target.addMouseListener(listener);

    }

    private static JMenuItem moveToEntry(@NotNull final Alignment a,
                                         @NotNull final TabFrameTabComponent target,
                                         @NotNull final Map<Alignment, JMenuItem> others) {
        var item = new JMenuItem();
        item.setAction(new AbstractAction(moveToLabel(a), moveToIcon(a)) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                others.get(target.getAlignment()).setEnabled(true);
                target.moveTo(a);
                item.setEnabled(false);
            }
        });
        item.setDisabledIcon(moveToIcon(a).getDual());
        if (a == target.getAlignment()) {
            item.setEnabled(false);
        }
        return item;
    }

    @Contract(pure = true)
    private static String moveToLabel(final Alignment a) {
        return switch (a) {
            case NORTH -> "Top Left";
            case SOUTH -> "Bottom Right";
            case EAST -> "Right Top";
            case WEST -> "Left Bottom";
            case NORTH_EAST -> "Top Right";
            case NORTH_WEST -> "Left Top";
            case SOUTH_EAST -> "Right Bottom";
            case SOUTH_WEST -> "Bottom Left";
            case CENTER -> "";
        };
    }

    @Contract(pure = true)
    private static UIAwareIcon moveToIcon(final Alignment a) {
        return switch (a) {
            case NORTH -> Icons.MOVE_TOP_LEFT;
            case SOUTH -> Icons.MOVE_BOTTOM_RIGHT;
            case EAST -> Icons.MOVE_RIGHT_TOP;
            case WEST -> Icons.MOVE_LEFT_BOTTOM;
            case NORTH_EAST -> Icons.MOVE_TOP_RIGHT;
            case NORTH_WEST -> Icons.MOVE_LEFT_TOP;
            case SOUTH_EAST -> Icons.MOVE_RIGHT_BOTTOM;
            case SOUTH_WEST -> Icons.MOVE_BOTTOM_LEFT;
            case CENTER -> Icons.STOP;
        };
    }
}
