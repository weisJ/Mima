package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

/**
 * Drag Source Listener for {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorDragListener implements DragSourceListener, DragGestureListener {

    private final EditorTabbedPane tabbedPane;
    private boolean dragging;

    @Contract(pure = true)
    public EditorDragListener(final EditorTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        dragging = false;
    }

    @Override
    public void dragEnter(@NotNull final DragSourceDragEvent e) {
        checkExit();
        e.getDragSourceContext().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void dragOver(final DragSourceDragEvent e) {
        checkExit();
        final TabTransferData data = DnDUtil.getTabTransferData(e);
        if (data == null) {
            e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }
    }

    public void dropActionChanged(final DragSourceDragEvent e) {
    }

    @Override
    public void dragExit(@NotNull final DragSourceEvent e) {
        e.getDragSourceContext().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        tabbedPane.glassPane.showDrag(true);
        tabbedPane.dropTargetIndex = -1;
        tabbedPane.glassPane.setPoint(new Point(-1000, -1000));
        final var p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, tabbedPane.glassPane);
        tabbedPane.glassPane.setMouseLocation(p);
    }

    @Override
    public void dragDropEnd(final DragSourceDropEvent e) {
        tabbedPane.glassPane.showDrag(false);
        tabbedPane.glassPane.setImage(null);
        tabbedPane.getTabComponentAt(tabbedPane.dropSourceIndex).setVisible(true);
        dragging = false;
        tabbedPane.dropTargetIndex = -1;
        tabbedPane.dropSourceIndex = -1;
    }

    private void checkExit() {
        final var p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, tabbedPane);
        final var show = !tabbedPane.getTabAreaBound().contains(p);
        if (show) {
            tabbedPane.dropTargetIndex = -1;
        }
        tabbedPane.glassPane.showDrag(show);
    }

    @Override
    public void dragGestureRecognized(@NotNull final DragGestureEvent dge) {
        final Point tabPt = dge.getDragOrigin();
        final int dragTabIndex = tabbedPane.indexAtLocation(tabPt.x, tabPt.y);
        if (dragTabIndex < 0) {
            return;
        }
        tabbedPane.initGlassPane(dge.getComponent(), dge.getDragOrigin(), dragTabIndex);
        try {
            dragging = true;
            dge.startDrag(DragSource.DefaultMoveDrop,
                          new TabTransferable(tabbedPane, dragTabIndex),
                          EditorDragListener.this);
        } catch (final InvalidDnDOperationException ignored) {
        }
    }

    /**
     * Return whether dragging is in process.
     *
     * @return true if dragging.
     */
    public boolean isDragging() {
        return dragging;
    }
}
