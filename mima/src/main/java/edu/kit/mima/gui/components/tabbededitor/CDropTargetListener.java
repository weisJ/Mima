package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Objects;
import javax.swing.Icon;

/**
 * Target Listener for {@link EditorTabbedPane} dragging events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CDropTargetListener implements DropTargetListener {
    private final EditorTabbedPane tabbedPane;

    /*default*/ CDropTargetListener(final EditorTabbedPane tabbedPane) {
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
        if (tabbedPane.hasGhost()) {
            EditorTabbedPane.glassPane.setPoint(tabbedPane.buildGhostLocation(e.getLocation()));
            EditorTabbedPane.glassPane.repaint();
        }
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
        if (sourceIndex < 0) {
            return;
        }

        final Component cmp = source.getComponentAt(sourceIndex);
        final String str = source.getTitleAt(sourceIndex);
        final Icon icon = source.getIconAt(sourceIndex);
        final String tooltip = source.getToolTipTextAt(sourceIndex);
        if (tabbedPane != source) {
            source.remove(sourceIndex);

            if (targetIndex == tabbedPane.getTabCount()) {
                tabbedPane.addTab(str, cmp);
            } else {
                if (targetIndex < 0) {
                    targetIndex = 0;
                }
                tabbedPane.insertTab(str, icon, cmp, tooltip, targetIndex);
            }
            tabbedPane.setSelectedComponent(cmp);
            return;
        }

        if (targetIndex < 0 || sourceIndex == targetIndex) {
            return;
        }

        if (targetIndex == tabbedPane.getTabCount()) {
            source.remove(sourceIndex);
            tabbedPane.addTab(str, icon, cmp, tooltip);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        } else if (sourceIndex > targetIndex) {
            source.remove(sourceIndex);
            tabbedPane.insertTab(str, icon, cmp, tooltip, targetIndex);
            tabbedPane.setSelectedIndex(targetIndex);
        } else {
            source.remove(sourceIndex);
            tabbedPane.insertTab(str, icon, cmp, tooltip, targetIndex - 1);
            tabbedPane.setSelectedIndex(targetIndex - 1);
        }
    }

    /**
     * Returns whether the target of the given event supports dragging.
     *
     * @param e DropTarget Drag Event.
     * @return true if target supports dragging.
     */
    public boolean isDragAcceptable(@NotNull final DropTargetDragEvent e) {
        if (!isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors())) {
            return false;
        } else {
            return checkDropAcceptable(Objects.requireNonNull(DnDUtil.getTabTransferData(e)));
        }
    }

    /**
     * Returns whether the target of the given event supports dropping.
     *
     * @param e DropTarget Drop Event.
     * @return true if target supports dropping.
     */
    public boolean isDropAcceptable(@NotNull final DropTargetDropEvent e) {
        if (!isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors())) {
            return false;
        } else {
            return checkDropAcceptable(Objects.requireNonNull(DnDUtil.getTabTransferData(e)));
        }
    }

    @Contract("null, _ -> false")
    private boolean isDoDAcceptable(@Nullable final Transferable t, final DataFlavor[] flavor) {
        if (t == null) {
            return false;
        }
        return t.isDataFlavorSupported(flavor[0]);
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
