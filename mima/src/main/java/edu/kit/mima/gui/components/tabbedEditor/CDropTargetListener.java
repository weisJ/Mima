package edu.kit.mima.gui.components.tabbedEditor;

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
 * @author Jannis Weis
 * @since 2018
 */
public class CDropTargetListener implements DropTargetListener {
    private EditorTabbedPane tabbedPane;

    /*default*/ CDropTargetListener(EditorTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void dragEnter(DropTargetDragEvent e) {
        if (isDragAcceptable(e)) {
            e.acceptDrag(e.getDropAction());
        } else {
            e.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent e) {
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    }

    public void dragOver(final DropTargetDragEvent e) {
        tabbedPane.initTarget(e.getLocation());

        tabbedPane.repaint();
        if (tabbedPane.hasGhost()) {
            EditorTabbedPane.glassPane.setPoint(tabbedPane.buildGhostLocation(e.getLocation()));
            EditorTabbedPane.glassPane.repaint();
        }
    }

    public void drop(DropTargetDropEvent event) {
        if (isDropAcceptable(event)) {
            convertTab(Objects.requireNonNull(DnDUtil.getTabTransferData(event)),
//                    tabbedPane.getTargetTabIndex(event.getLocation()))
                    tabbedPane.dropTargetIndex);
            event.dropComplete(true);
        } else {
            event.dropComplete(false);
        }
        tabbedPane.repaint();
    }

    private void convertTab(TabTransferData transferData, int targetIndex) {
        EditorTabbedPane source = transferData.getTabbedPane();
        int sourceIndex = transferData.getTabIndex();
        if (sourceIndex < 0) {
            return;
        }

        Component cmp = source.getComponentAt(sourceIndex);
        String str = source.getTitleAt(sourceIndex);
        Icon icon = source.getIconAt(sourceIndex);
        String tooltip = source.getToolTipTextAt(sourceIndex);
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

    public boolean isDragAcceptable(DropTargetDragEvent e) {
        if (!isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors())) {
            return false;
        } else {
            return checkDropAcceptable(Objects.requireNonNull(DnDUtil.getTabTransferData(e)));
        }
    }

    public boolean isDropAcceptable(DropTargetDropEvent e) {
        if (!isDoDAcceptable(e.getTransferable(), e.getCurrentDataFlavors())) {
            return false;
        } else {
            return checkDropAcceptable(Objects.requireNonNull(DnDUtil.getTabTransferData(e)));
        }
    }

    private boolean isDoDAcceptable(Transferable t, DataFlavor[] flavor) {
        if (t == null) {
            return false;
        }
        return t.isDataFlavorSupported(flavor[0]);
    }

    private boolean checkDropAcceptable(TabTransferData data) {
        if (tabbedPane == data.getTabbedPane()
                && data.getTabIndex() >= 0) {
            return true;
        }
        if (tabbedPane != data.getTabbedPane()) {
            if (tabbedPane.getAcceptor() != null) {
                return tabbedPane.getAcceptor().isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
            }
        }
        return false;
    }
}
