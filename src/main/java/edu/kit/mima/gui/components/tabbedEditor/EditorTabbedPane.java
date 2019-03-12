package edu.kit.mima.gui.components.tabbedEditor;

import edu.kit.mima.gui.util.HSLColor;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.geom.Rectangle2D;
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
    private static final int LINE_WIDTH = 3;
    /*default*/ static GhostGlassPane glassPane = new GhostGlassPane();
    private final Rectangle2D lineRect = new Rectangle2D.Double();
    private final Color lineColor;
    private final Color selectedColor;
    private final Color tabBorderColor;
    private final Color selectedBackground;
    private final List<TabClosedEventHandler> handlerList;

    /*default*/ boolean isDrawRect = false;
    private boolean hasGhost = true;
    private TabAcceptor tabAcceptor;

    public EditorTabbedPane() {
        var c = UIManager.getColor("TabbedPane.separatorHighlight");
        lineColor = c == null ? UIManager.getColor("TabbedPane.selected") : c;
        selectedColor = new HSLColor(lineColor).adjustTone(10).adjustSaturation(40).getRGB();
        tabBorderColor = new HSLColor(getBackground()).adjustTone(10).getRGB();
        selectedBackground = new HSLColor(getBackground()).adjustTone(20).adjustSaturation(5).getRGB();
        handlerList = new ArrayList<>();
        setFocusable(false); //Prevent focus dotted line from appearing
        final DragSourceListener dsl = new DragSourceListener() {
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext()
                        .setCursor(DragSource.DefaultMoveNoDrop);
                lineRect.setRect(0, 0, 0, 0);
                isDrawRect = false;
                glassPane.setPoint(new Point(-1000, -1000));
                glassPane.repaint();
            }

            public void dragOver(DragSourceDragEvent e) {
                TabTransferData data = DnDUtil.getTabTransferData(e);
                if (data == null) {
                    e.getDragSourceContext().setCursor(
                            DragSource.DefaultMoveNoDrop);
                    return;
                }
                e.getDragSourceContext().setCursor(
                        DragSource.DefaultMoveDrop);
            }

            public void dragDropEnd(DragSourceDropEvent e) {
                isDrawRect = false;
                lineRect.setRect(0, 0, 0, 0);
                if (hasGhost()) {
                    glassPane.setVisible(false);
                    glassPane.setImage(null);
                }
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
        final ButtonClose buttonClose = new ButtonClose(title, icon, this::closeTab);
        setTabComponentAt(index, buttonClose);
        setSelectedIndex(index);
    }

    private void closeTab(ButtonClose sender) {
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

    public void setPaintGhost(boolean flag) {
        hasGhost = flag;
    }

    public boolean hasGhost() {
        return hasGhost;
    }

    /*default*/ Point buildGhostLocation(Point location) {
        Point ghostLocation = new Point(location);

        switch (getTabPlacement()) {
            case JTabbedPane.TOP: {
                ghostLocation.y = 1;
                ghostLocation.x -= glassPane.getGhostWidth() / 2;
            }
            break;

            case JTabbedPane.BOTTOM: {
                ghostLocation.y = getHeight() - 1 - glassPane.getGhostHeight();
                ghostLocation.x -= glassPane.getGhostWidth() / 2;
            }
            break;

            case JTabbedPane.LEFT: {
                ghostLocation.x = 1;
                ghostLocation.y -= glassPane.getGhostHeight() / 2;
            }
            break;

            case JTabbedPane.RIGHT: {
                ghostLocation.x = getWidth() - 1 - glassPane.getGhostWidth();
                ghostLocation.y -= glassPane.getGhostHeight() / 2;
            }
            break;
        }

        ghostLocation = SwingUtilities.convertPoint(EditorTabbedPane.this, ghostLocation, glassPane);
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

    /*default*/ void initTargetLeftRightLine(int next, TabTransferData transferData) {
        if (next < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((transferData.getTabbedPane() == this)
                && (transferData.getTabIndex() == next
                            || next - transferData.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(-LINE_WIDTH / 2, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect((rect.x + rect.width) - (LINE_WIDTH / 2), rect.y,
                    LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            lineRect.setRect((rect.x + rect.width) - (LINE_WIDTH / 2), rect.y,
                    LINE_WIDTH, rect.height);
            isDrawRect = true;
        }
    }

    /*default*/ void initTargetTopBottomLine(int next, TabTransferData a_data) {
        if (next < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((a_data.getTabbedPane() == this)
                && (a_data.getTabIndex() == next
                            || next - a_data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x, (rect.y + rect.height) - (LINE_WIDTH / 2),
                    rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(rect.x, -LINE_WIDTH / 2, rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            lineRect.setRect(rect.x, (rect.y + rect.height) - (LINE_WIDTH / 2),
                    rect.width, LINE_WIDTH);
            isDrawRect = true;
        }
    }

    private void initGlassPane(Component c, Point tabPt, int a_tabIndex) {
        getRootPane().setGlassPane(glassPane);
        if (hasGhost()) {
            Rectangle rect = getBoundsAt(a_tabIndex);
            BufferedImage image = new BufferedImage(c.getWidth(),
                    c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            c.paint(g);
            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            glassPane.setImage(image);
        }
        glassPane.setPoint(buildGhostLocation(tabPt));
        glassPane.setVisible(true);
    }

    private Rectangle getTabAreaBound() {
        Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
        return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
    }

    private boolean isStacked() {
        if (getTabCount() <= 0) {
            return false;
        }
        var boundsArea = getTabAreaBound();
        var boundsTab = getBoundsAt(0);
        switch (getTabPlacement()) {
            case JTabbedPane.TOP:
            case JTabbedPane.BOTTOM:
                return boundsArea.height > boundsTab.height;
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                return boundsArea.width > boundsTab.width;
        }
        return false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isDrawRect) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(lineColor);
            g2.fill(lineRect);
        }

        for (int i = 0; i < getTabCount(); i++) {
            var bounds = getBoundsAt(i);
            int yOff = bounds.height / 6;
            if (i == getSelectedIndex()) {
                g.setColor(selectedBackground);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g.setColor(selectedColor);
                g.fillRect(bounds.x, bounds.y + bounds.height - yOff, bounds.width, yOff);
            } else {
                g.setColor(getBackground());
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            g.setColor(tabBorderColor);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void setSelectedTitle(String title) {
        ((ButtonClose) getTabComponentAt(getSelectedIndex())).setTitle(title);
    }
}
