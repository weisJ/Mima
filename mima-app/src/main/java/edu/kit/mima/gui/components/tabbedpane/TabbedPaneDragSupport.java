package edu.kit.mima.gui.components.tabbedpane;

import edu.kit.mima.gui.components.dragging.DragListener;
import edu.kit.mima.gui.components.dragging.SnapDraggingSupport;
import edu.kit.mima.gui.components.dragging.SnapListener;
import edu.kit.mima.gui.components.text.editor.Editor;
import edu.kit.mima.util.ImageUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

/**
 * Drag Source Listener for {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabbedPaneDragSupport
        implements DragSourceListener, DragGestureListener, SnapListener, DragListener {

    private final DnDTabbedPane tabbedPane;
    private final SnapDraggingSupport draggingSupport;
    private boolean dragging;
    private int dropTargetIndex = -1;
    private int dropSourceIndex = -1;

    @Contract(pure = true)
    public TabbedPaneDragSupport(final DnDTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        dragging = false;
        draggingSupport =
                new SnapDraggingSupport(
                        tabbedPane,
                        () -> {
                            var rect = tabbedPane.getTabAreaBound();
                            rect.setRect(rect.x, rect.y + 1, rect.width, rect.height);
                            return rect;
                        },
                        1.0f,
                        0.9f,
                        20);
        draggingSupport.addSnapListener(this);
        draggingSupport.addDragListener(this);
        new DragSource()
                .createDefaultDragGestureRecognizer(tabbedPane, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    @Override
    public void dragEnter(@NotNull final DragSourceDragEvent e) {
    }

    @Override
    public void dragOver(@NotNull final DragSourceDragEvent e) {
    }

    public void dropActionChanged(final DragSourceDragEvent e) {
    }

    @Override
    public void dragExit(@NotNull final DragSourceEvent e) {
    }

    @Override
    public void dragDropEnd(final DragSourceDropEvent e) {
        tabbedPane.getTabComponentAt(dropSourceIndex).setVisible(true);
        draggingSupport.showDrag(false);
        dragging = false;
        drop();
        dropTargetIndex = -1;
        dropSourceIndex = -1;
    }

    private void drop() {
        int newIndex = dropSourceIndex > dropTargetIndex ? dropTargetIndex : dropTargetIndex - 1;
        if (newIndex < 0 || dropTargetIndex < 0 || newIndex == dropSourceIndex) {
            return;
        }
        final Component cmp = tabbedPane.getComponentAt(dropSourceIndex);
        final String str = tabbedPane.getTitleAt(dropSourceIndex);
        final Icon icon = tabbedPane.getIconAt(dropSourceIndex);
        final String tooltip = tabbedPane.getToolTipTextAt(dropSourceIndex);
        tabbedPane.remove(dropSourceIndex);
        tabbedPane.insertTab(str, icon, cmp, tooltip, newIndex);
        tabbedPane.setSelectedIndex(newIndex);
    }

    @Override
    public void dragGestureRecognized(@NotNull final DragGestureEvent dge) {
        final Point tabPt = dge.getDragOrigin();
        final int dragTabIndex = tabbedPane.indexAtLocation(tabPt.x, tabPt.y);
        if (dragTabIndex < 0) {
            return;
        }
        initDragPane(dge.getComponent(), dragTabIndex);
        try {
            dragging = true;
            draggingSupport.showDrag(true);
            dge.startDrag(
                    Cursor.getDefaultCursor(),
                    new TabTransferable(tabbedPane, dragTabIndex),
                    TabbedPaneDragSupport.this);
        } catch (@NotNull final InvalidDnDOperationException ignored) {
        }
    }

    private void initDragPane(@NotNull final Component c, final int tabIndex) {
        final Rectangle compRect = tabbedPane.getComponentAt(tabIndex).getBounds();
        final var comp = tabbedPane.getComponentAt(tabIndex);
        Image compImage;
        var bounds = tabbedPane.getBoundsAt(tabIndex);
        bounds.x += 1;
        bounds.y += 1;
        Image tabImage = ImageUtil.imageFromComponent(c, tabbedPane.getBoundsAt(tabIndex));
        if (comp instanceof Editor) {
            compImage = ((Editor) comp).createPreviewImage();
        } else {
            compImage =
                    ImageUtil.imageFromComponent(
                            c,
                            new Rectangle(
                                    compRect.x,
                                    compRect.y,
                                    Math.max(compRect.width, 200),
                                    Math.max(compRect.height, 400)));
        }
        draggingSupport.setImage(tabImage);
        draggingSupport.setExtendedImage(compImage);
        dropSourceIndex = tabIndex;
        dropTargetIndex = tabIndex + 1;
        tabbedPane.getTabComponentAt(tabIndex).setVisible(false);
    }

    /**
     * Return whether dragging is in process.
     *
     * @return true if dragging.
     */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * Get the index of the drop source.
     *
     * @return the drop source index.
     */
    public int getDropSourceIndex() {
        return dropSourceIndex;
    }

    /**
     * Get the index of the drop target.
     *
     * @return the drop target index.
     */
    public int getDropTargetIndex() {
        return dropTargetIndex;
    }

    private void setDropTargetIndex(final int dropTargetIndex) {
        this.dropTargetIndex = dropTargetIndex;
        tabbedPane.repaint();
    }

    @Override
    public void onExit(final Point mouseLocation) {
        setDropTargetIndex(-1);
    }

    @Override
    public void onEnter(final Point mouseLocation) {
        onDrag(mouseLocation);
    }

    @Override
    public void onDrag(final Point mouseLocation) {
        if (draggingSupport.isSnapped()) {
            setDropTargetIndex(getTabIndexUnder(mouseLocation));
        }
    }

    /**
     * Returns the index under the given point.
     *
     * @param point point given in component coordinates.
     * @return index under point or -1 if none was found.
     */
    public int getTabIndexUnder(@NotNull final Point point) {
        if (tabbedPane.getTabCount() == 0) {
            return 0;
        }
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (i == dropSourceIndex) {
                continue;
            }
            var rect = tabbedPane.getBoundsAt(i);
            if (i == dropTargetIndex) {
                var r = tabbedPane.getBoundsAt(dropSourceIndex);
                rect.x -= r.width;
                rect.width += r.width;
            }
            if (i >= dropTargetIndex && dropTargetIndex >= 0) {
                var r = tabbedPane.getBoundsAt(dropSourceIndex);
                rect.x -= r.width / 2;
            }
            rect.y -= 20;
            rect.height += 40;
            if (rect.contains(point)) {
                return i;
            }
        }
        var rect = tabbedPane.getBoundsAt(tabbedPane.getTabCount() - 1);
        rect.setRect(
                rect.x + rect.width / 2,
                rect.y,
                tabbedPane.getWidth() - (rect.x + rect.width / 2),
                rect.height);
        rect.y -= 20;
        rect.height += 40;
        return rect.contains(point) ? tabbedPane.getTabCount() : -1;
    }
}
