package edu.kit.mima.gui.components.tabbedEditor;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Graphics;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class EditorTabbedPane extends JTabbedPane {
    /*default*/ static final String NAME = "TabTransferData";
    /*default*/ static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    public static String SELECTED_TAB_PROPERTY = "selectedTab";
    /*default*/ static GhostGlassPane glassPane = new GhostGlassPane();
    private final List<TabClosedEventHandler> handlerList;
    /*default*/ int dropTargetIndex = -1;
    /*default*/ int dropSourceIndex = -1;
    private int selectedTab;
    private boolean dragging = false;
    private boolean hasGhost = true;
    private TabAcceptor tabAcceptor;

    public EditorTabbedPane() {
        setUI(new CustomTabbedPaneUI(this));

        handlerList = new ArrayList<>();
        setFocusable(false); //Prevent focus dotted line from appearing
        setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        addChangeListener(e -> {
            firePropertyChange(SELECTED_TAB_PROPERTY, selectedTab, getSelectedIndex());
            selectedTab = getSelectedIndex();
        });
        final DragSourceListener dsl = new DragSourceListener() {
            public void dragEnter(DragSourceDragEvent e) {
                checkExit(e);
            }

            private void checkExit(DragSourceEvent e) {
                var p = e.getLocation();
                SwingUtilities.convertPointFromScreen(p, EditorTabbedPane.this);
                var show = !getTabAreaBound().contains(p);
                if (show) {
                    dropTargetIndex = -1;
                }
                glassPane.showDrag(show);
            }

            public void dragExit(DragSourceEvent e) {
                glassPane.showDrag(true);
                dropTargetIndex = -1;
                glassPane.setPoint(new Point(-1000, -1000));
                var p = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(p, glassPane);
                glassPane.setMouseLocation(p);
            }

            public void dragOver(DragSourceDragEvent e) {
                checkExit(e);
                TabTransferData data = DnDUtil.getTabTransferData(e);
                if (data == null) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                }
            }

            public void dragDropEnd(DragSourceDropEvent e) {
                if (hasGhost()) {
                    glassPane.showDrag(false);
                    glassPane.setImage(null);
                }
                getTabComponentAt(dropSourceIndex).setVisible(true);
                dragging = false;
                dropTargetIndex = -1;
                dropSourceIndex = -1;
            }

            public void dropActionChanged(DragSourceDragEvent e) {
            }
        };

        final DragGestureListener dgl = e -> {
            Point tabPt = e.getDragOrigin();
            int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
            if (dragTabIndex < 0) {
                return;
            }
            initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
            try {
                dragging = true;
                e.startDrag(DragSource.DefaultMoveDrop,
                        new TabTransferable(EditorTabbedPane.this, dragTabIndex), dsl);
            } catch (InvalidDnDOperationException ex) {
                ex.printStackTrace();
            }
        };

        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
                new CDropTargetListener(this), true);
        new DragSource().createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, dgl);
        tabAcceptor = (component, index) -> true;
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        super.insertTab(title, icon, component, tip, index);
        final TabComponent tabComponent = new TabComponent(title, icon, this::closeTab);
        setTabComponentAt(index, tabComponent);
        setSelectedIndex(index);
    }

    /**
     * Close a tab
     *
     * @param sender tab component that sends the close request.
     */
    private void closeTab(TabComponent sender) {
        int i = indexOfTabComponent(sender);
        for (var handler : handlerList) {
            if (handler != null) {
                handler.tabClosed(getComponentAt(i));
            }
        }
        if (i != -1) {
            remove(i);
        }
    }

    /**
     * Add {@link TabClosedEventHandler}
     *
     * @param handler handler to add
     */
    public void addTabClosedEventHandler(TabClosedEventHandler handler) {
        handlerList.add(handler);
    }

    /**
     * Remove {@link TabClosedEventHandler}}
     *
     * @param handler handler to remove
     * @return true if removed successfully
     */
    public boolean removeTabClosedEventHandler(TabClosedEventHandler handler) {
        return handlerList.remove(handler);
    }

    /**
     * Get the {@link TabAcceptor}
     *
     * @return current {@link TabAcceptor}
     */
    public TabAcceptor getAcceptor() {
        return tabAcceptor;
    }

    /**
     * Set the {@link TabAcceptor}
     *
     * @param acceptor acceptor to set.
     */
    public void setAcceptor(TabAcceptor acceptor) {
        tabAcceptor = acceptor;
    }

    /**
     * Set whether tp paint the ghost image when dragging.
     *
     * @param flag true if ghost should be painted.
     */
    public void setPaintGhost(boolean flag) {
        hasGhost = flag;
    }

    /**
     * Returns whether a ghost image is painted while dragging.
     *
     * @return true if painted.
     */
    public boolean hasGhost() {
        return hasGhost;
    }

    /**
     * Set the title for the selected tab.
     *
     * @param title new title
     */
    public void setSelectedTitle(String title) {
        ((TabComponent) getTabComponentAt(getSelectedIndex())).setTitle(title);
    }

    /*default*/ Point buildGhostLocation(Point location) {
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
        }
        ghostLocation = SwingUtilities.convertPoint(EditorTabbedPane.this, ghostLocation, glassPane);
        ghostLocation.x = Math.min(Math.max(getX(), ghostLocation.x), getX() + getWidth() - glassPane.getGhostWidth());
        ghostLocation.y = Math.min(Math.max(getY(), ghostLocation.y), getY() + getHeight() - glassPane.getGhostHeight());
        return ghostLocation;
    }

    /**
     * returns potential index for drop.
     *
     * @param point point given in the drop site component's coordinate
     * @return returns potential index for drop.
     */
    /*default*/ int getTargetTabIndex(Point point) {
        boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP
                || getTabPlacement() == JTabbedPane.BOTTOM;
        if (getTabCount() == 0) {
            return 0;
        }

        for (int i = 0; i < getTabCount(); i++) {
            Rectangle r = getBoundsAt(i);
            if (isTopOrBottom) {
                r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
            } else {
                r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
            }

            if (r.contains(point)) {
                return i;
            }
        }

        Rectangle r = getBoundsAt(getTabCount() - 1);
        if (isTopOrBottom) {
            int x = r.x + r.width / 2;
            r.setRect(x, r.y, getWidth() - x, r.height);
        } else {
            int y = r.y + r.height / 2;
            r.setRect(r.x, y, r.width, getHeight() - y);
        }

        return r.contains(point) ? getTabCount() : -1;
    }

    private void initGlassPane(Component c, Point tabPos, int tabIndex) {
        getRootPane().setGlassPane(glassPane);
        if (hasGhost()) {
            Rectangle rect = getBoundsAt(tabIndex);
            BufferedImage image = new BufferedImage(c.getWidth(),
                    c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            c.paint(g);
            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            glassPane.setImage(image);
            dropSourceIndex = tabIndex;
            dropTargetIndex = tabIndex;
            getTabComponentAt(tabIndex).setVisible(false);
        }
        glassPane.setPoint(buildGhostLocation(tabPos));
        glassPane.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        getUI().paint(g, this);
        super.paint(g);
    }

    private Rectangle getTabAreaBound() {
        Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
        return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
    }

    /*default*/ void initTarget(Point location) {
        int newLocation = getTargetTabIndex(location);
        if (newLocation < 0 && dragging) {
            return;
        }
        dropTargetIndex = newLocation;
    }
}
