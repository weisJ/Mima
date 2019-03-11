package edu.kit.mima.gui.components.tabbedEditor;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class DnDUtil {
    private DnDUtil() {
        assert false : "Utility class constructor";
    }

    public static TabTransferData getTabTransferData(DropTargetDropEvent a_event) {
        try {
            return (TabTransferData) a_event.getTransferable().getTransferData(EditorTabbedPane.FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TabTransferData getTabTransferData(DropTargetDragEvent a_event) {
        try {
            return (TabTransferData) a_event.getTransferable().getTransferData(EditorTabbedPane.FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TabTransferData getTabTransferData(DragSourceDragEvent a_event) {
        try {
            return (TabTransferData) a_event.getDragSourceContext().getTransferable().getTransferData(EditorTabbedPane.FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
