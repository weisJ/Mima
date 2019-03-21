package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * Utility class for Drag and Drop.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class DnDUtil {
    @Contract(" -> fail")
    private DnDUtil() {
        assert false : "Utility class constructor";
    }

    /**
     * Get Tab TransferData from DropTargetDropEvent.
     *
     * @param event DropTargetDropEvent.
     * @return TabTransferData
     */
    @Nullable
    public static TabTransferData getTabTransferData(@NotNull final DropTargetDropEvent event) {
        try {
            return (TabTransferData) event.getTransferable()
                    .getTransferData(EditorTabbedPane.FLAVOR);
        } catch (@NotNull final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get Tab TransferData from DropTargetDragEvent.
     *
     * @param event DropTargetDragEvent.
     * @return TabTransferData
     */
    @Nullable
    public static TabTransferData getTabTransferData(@NotNull final DropTargetDragEvent event) {
        try {
            return (TabTransferData) event.getTransferable()
                    .getTransferData(EditorTabbedPane.FLAVOR);
        } catch (@NotNull final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get Tab TransferData from DragSourceEvent.
     *
     * @param event DragSourceEvent.
     * @return TabTransferData
     */
    @Nullable
    public static TabTransferData getTabTransferData(@NotNull final DragSourceDragEvent event) {
        try {
            return (TabTransferData) event.getDragSourceContext().getTransferable()
                    .getTransferData(EditorTabbedPane.FLAVOR);
        } catch (@NotNull final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
