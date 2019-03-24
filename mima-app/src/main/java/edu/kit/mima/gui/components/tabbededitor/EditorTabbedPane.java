package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.util.ImageUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Tabbed Pane with custom UI.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorTabbedPane extends JTabbedPane {
    public static final String SELECTED_TAB_PROPERTY = "selectedTab";
    /*default*/ static final String NAME = "TabTransferData";
    /*default*/ static final DataFlavor FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType, NAME);
    /*default*/ final DraggingGlassPane glassPane;
    private final List<TabClosedEventHandler> handlerList;
    /*default*/ int dropTargetIndex = -1;
    /*default*/ int dropSourceIndex = -1;
    private int selectedTab;
    private boolean dragging = false;
    private TabAcceptor tabAcceptor;

    /**
     * Create new Editor Tabbed Pane.
     */
    public EditorTabbedPane() {
        setUI(new CustomTabbedPaneUI(this));

        glassPane = new DraggingGlassPane(this);
        handlerList = new ArrayList<>();
        setFocusable(false); //Prevent focus dotted line from appearing
        setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        addChangeListener(e -> {
            firePropertyChange(SELECTED_TAB_PROPERTY, selectedTab, getSelectedIndex());
            selectedTab = getSelectedIndex();
        });
        final DragSourceListener dsl = new DragSourceListener() {
            public void dragEnter(@NotNull final DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                checkExit(e);
            }

            private void checkExit(@NotNull final DragSourceEvent e) {
                final var p = e.getLocation();
                SwingUtilities.convertPointFromScreen(p, EditorTabbedPane.this);
                final var show = !getTabAreaBound().contains(p);
                if (show) {
                    dropTargetIndex = -1;
                }
                glassPane.showDrag(show);
            }

            public void dragExit(@NotNull final DragSourceEvent e) {
                e.getDragSourceContext().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                glassPane.showDrag(true);
                dropTargetIndex = -1;
                glassPane.setPoint(new Point(-1000, -1000));
                final var p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, glassPane);
                glassPane.setMouseLocation(p);
            }

            public void dragOver(final DragSourceDragEvent e) {
                checkExit(e);
                final TabTransferData data = DnDUtil.getTabTransferData(e);
                if (data == null) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                }
            }

            public void dragDropEnd(final DragSourceDropEvent e) {
                glassPane.showDrag(false);
                glassPane.setImage(null);
                getTabComponentAt(dropSourceIndex).setVisible(true);
                dragging = false;
                dropTargetIndex = -1;
                dropSourceIndex = -1;
            }

            public void dropActionChanged(final DragSourceDragEvent e) {
            }
        };

        final DragGestureListener dgl = e -> {
            final Point tabPt = e.getDragOrigin();
            final int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
            if (dragTabIndex < 0) {
                return;
            }
            initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
            try {
                dragging = true;
                e.startDrag(DragSource.DefaultMoveDrop,
                            new TabTransferable(EditorTabbedPane.this, dragTabIndex), dsl);
            } catch (final InvalidDnDOperationException ignored) {
            }
        };

        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
                       new CDropTargetListener(this), true);
        new DragSource().createDefaultDragGestureRecognizer(this,
                                                            DnDConstants.ACTION_COPY_OR_MOVE, dgl);
        tabAcceptor = (component, index) -> true;
    }

    @Override
    public void insertTab(final String title, final Icon icon,
                          final Component component, final String tip, final int index) {
        super.insertTab(title, icon, component, tip, index);
        final TabComponent tabComponent = new TabComponent(title, icon, this::closeTab);
        setTabComponentAt(index, tabComponent);
        setSelectedIndex(index);
    }

    /**
     * Close a tab.
     *
     * @param sender tab component that sends the close request.
     */
    private void closeTab(final TabComponent sender) {
        final int i = indexOfTabComponent(sender);
        for (final var handler : handlerList) {
            if (handler != null) {
                handler.tabClosed(getComponentAt(i));
            }
        }
        if (i != -1) {
            remove(i);
        }
    }

    /**
     * Add {@link TabClosedEventHandler}.
     *
     * @param handler handler to add
     */
    public void addTabClosedEventHandler(final TabClosedEventHandler handler) {
        handlerList.add(handler);
    }

    /**
     * Remove {@link TabClosedEventHandler}}.
     *
     * @param handler handler to remove
     * @return true if removed successfully
     */
    public boolean removeTabClosedEventHandler(final TabClosedEventHandler handler) {
        return handlerList.remove(handler);
    }

    /**
     * Get the {@link TabAcceptor}.
     *
     * @return current {@link TabAcceptor}
     */
    public TabAcceptor getAcceptor() {
        return tabAcceptor;
    }

    /**
     * Set the {@link TabAcceptor}.
     *
     * @param acceptor acceptor to set.
     */
    public void setAcceptor(final TabAcceptor acceptor) {
        tabAcceptor = acceptor;
    }


    Point buildGhostLocation(final Point location) {
        Point ghostLocation = new Point(location);
        switch (getTabPlacement()) {
            case JTabbedPane.TOP:
                ghostLocation.y = 0;
                ghostLocation.x -= glassPane.getGhostWidth() / 2;
                break;
            case JTabbedPane.BOTTOM:
                ghostLocation.y = getHeight() - 1 - glassPane.getGhostHeight();
                ghostLocation.x -= glassPane.getGhostWidth() / 2;
                break;
            case JTabbedPane.LEFT:
                ghostLocation.x = 0;
                ghostLocation.y -= glassPane.getGhostHeight() / 2;
                break;
            case JTabbedPane.RIGHT:
                ghostLocation.x = getWidth() - 1 - glassPane.getGhostWidth();
                ghostLocation.y -= glassPane.getGhostHeight() / 2;
                break;
            default:
                break;
        }
        ghostLocation = SwingUtilities
                .convertPoint(EditorTabbedPane.this, ghostLocation, glassPane);
        ghostLocation.x = Math.min(Math.max(getX(), ghostLocation.x),
                                   getX() + getWidth() - glassPane.getGhostWidth());
        ghostLocation.y = Math.min(Math.max(getY(), ghostLocation.y),
                                   getY() + getHeight() - glassPane.getGhostHeight());
        return ghostLocation;
    }

    /**
     * returns potential index for drop.
     *
     * @param point point given in the drop site component's coordinate
     * @return returns potential index for drop.
     */
    /*default*/ int getTargetTabIndex(final Point point) {
        if (getTabCount() == 0) {
            return 0;
        }
        for (int i = 0; i < getTabCount(); i++) {
            final Rectangle r = getBoundsAt(i);
            r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
            if (r.contains(point)) {
                return i;
            }
        }
        final Rectangle r = getBoundsAt(getTabCount() - 1);
        final int x = r.x + r.width / 2;
        r.setRect(x, r.y, getWidth() - x, r.height);
        return r.contains(point) ? getTabCount() : -1;
    }

    private void initGlassPane(@NotNull final Component c,
                               final Point tabPos,
                               final int tabIndex) {
        getRootPane().setGlassPane(glassPane);
        final Rectangle compRect = getComponentAt(tabIndex).getBounds();
        final var comp = getComponentAt(tabIndex);
        Image compImage;
        Image tabImage = ImageUtil.imageFromComponent(c, getBoundsAt(tabIndex));
        if (comp instanceof Editor) {
            compImage = ((Editor) comp).createPreviewImage();
        } else {
            compImage = ImageUtil.imageFromComponent(
                    c, new Rectangle(compRect.x, compRect.y, Math.max(compRect.width, 200),
                                     Math.max(compRect.height, 400)));
        }
        glassPane.setImage(tabImage);
        glassPane.setExtendedImage(compImage);
        dropSourceIndex = tabIndex;
        dropTargetIndex = tabIndex;
        getTabComponentAt(tabIndex).setVisible(false);
        glassPane.setPoint(buildGhostLocation(tabPos));
        glassPane.setVisible(true);
    }

    @Override
    public void paint(final Graphics g) {
        getUI().paint(g, this);
        super.paint(g);
    }

    @Override
    public void setTabLayoutPolicy(final int tabLayoutPolicy) {
        //No other supported at the moment.
    }

    @NotNull
    public Rectangle getTabAreaBound() {
        final Rectangle firstTab = getUI().getTabBounds(this, 0);
        return new Rectangle(0, 0, getWidth(), firstTab.y + firstTab.height);
    }

    /*default*/ void initTarget(final Point location) {
        final int newLocation = getTargetTabIndex(location);
        if (newLocation < 0 && dragging) {
            return;
        }
        dropTargetIndex = newLocation;
    }
}
