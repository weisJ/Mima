package edu.kit.mima.gui.context;

import edu.kit.mima.annotations.Context;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.folderdisplay.DirectoryTooltip;
import edu.kit.mima.gui.components.folderdisplay.FilePathDisplay;
import edu.kit.mima.gui.components.folderdisplay.FilePathDisplayItem;
import edu.kit.mima.gui.components.listeners.PopupListener;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

/**
 * Provider for FilePathDisplayItem contexts.
 *
 * @author Jannis Weis
 * @since 2019
 */
@Context(provides = FilePathDisplayItem.class)
public final class FileDisplayContextProvider extends CachedContextProvider {


    /**
     * Create and register a context menu for the given {@link FilePathDisplay}.
     *
     * @param target the target component.
     */
    @ReflectionCall
    public static void createContextMenu(@NotNull final FilePathDisplayItem target) {
        PopupListener cached = get(target);
        if (cached == null) {
            final var tooltip = new DirectoryTooltip(target.getFile(), target.getHandler());
            var popupListener = new PopupListener(tooltip, MouseEvent.BUTTON1, true);
            cache(target, popupListener);
            target.addMouseListener(popupListener);
        } else {
            target.removeMouseListener(cached);
            target.addMouseListener(cached);
        }
    }
}
