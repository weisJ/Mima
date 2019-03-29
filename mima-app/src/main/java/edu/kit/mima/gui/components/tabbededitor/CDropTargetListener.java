package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Objects;

/**
 * Target Listener for {@link EditorTabbedPane} dragging events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CDropTargetListener implements DropTargetListener {
    private final EditorTabbedPane tabbedPane;

    /*default*/
    @Contract(pure = true)
    CDropTargetListener(final EditorTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void dragEnter(@NotNull final DropTargetDragEvent e) {
        if (isDragAcceptable(e)) {
            e.acceptDrag(e.getDropAction());
        } else {
            e.rejectDrag();
        }
    }

    @Override
    public void dragExit(final DropTargetEvent e) {
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent e) {
    }

    @Override
    public void dragOver(@NotNull final DropTargetDragEvent e) {
        tabbedPane.initTarget(e.getLocation());
        tabbedPane.repaint();
        tabbedPane.glassPane.setPoint(tabbedPane.buildGhostLocation(e.getLocation()));
        tabbedPane.glassPane.repaint();
    }

    @Override
    public void drop(@NotNull final DropTargetDropEvent event) {
        if (isDropAcceptable(event)) {
            convertTab(Objects.requireNonNull(DnDUtil.getTabTransferData(event)),
                       tabbedPane.dropTargetIndex);
            event.dropComplete(true);
        } else {
            event.dropComplete(false);
        }
        tabbedPane.repaint();
    }

    private void convertTab(@NotNull final TabTransferData transferData, int targetIndex) {
        final EditorTabbedPane source = transferData.getTabbedPane();
        final int sourceIndex = transferData.getTabIndex();
        int newIndex = tabbedPane == source && sourceIndex > targetIndex
                       ? targetIndex
                       : targetIndex - 1;
        if (newIndex < 0
            || targetIndex < 0
            || (tabbedPane == source && newIndex == sourceIndex)) {
            return;
        }
        final Component cmp = source.getComponentAt(sourceIndex);
        final String str = source.getTitleAt(sourceIndex);
        final Icon icon = source.getIconAt(sourceIndex);
        final String tooltip = source.getToolTipTextAt(sourceIndex);
        source.remove(sourceIndex);
        tabbedPane.insertTab(str, icon, cmp, tooltip, newIndex);
        tabbedPane.setSelectedIndex(newIndex);
    }

    /**
     * Returns whether the target of the given event supports dragging.
     *
     * @param e DropTarget Drag Event.
     * @return true if target supports dragging.
     */
    public boolean isDragAcceptable(@NotNull final DropTargetDragEvent e) {
        return isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors(),
                               DnDUtil.getTabTransferData(e));
    }

    /**
     * Returns whether the target of the given event supports dropping.
     *
     * @param e DropTarget Drop Event.
     * @return true if target supports dropping.
     */
    public boolean isDropAcceptable(@NotNull final DropTargetDropEvent e) {
        return isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors(),
                               DnDUtil.getTabTransferData(e));
    }

    @Contract("null, _, _ -> false")
    private boolean isDoDAcceptable(@Nullable final Transferable t,
                                    final DataFlavor[] flavor,
                                    final TabTransferData data) {
        if (t == null || !t.isDataFlavorSupported(flavor[0])) {
            return false;
        } else {
            return checkDropAcceptable(Objects.requireNonNull(data));
        }
    }

    private boolean checkDropAcceptable(@NotNull final TabTransferData data) {
        if (tabbedPane == data.getTabbedPane()
                && data.getTabIndex() >= 0) {
            return true;
        }
        if (tabbedPane != data.getTabbedPane()) {
            if (tabbedPane.getAcceptor() != null) {
                return tabbedPane.getAcceptor()
                        .isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
        }
        return false;
    }
}
