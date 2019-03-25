package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.util.ImageUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
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
    static final String NAME = "TabTransferData";
    static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

    final DraggingGlassPane glassPane;
    private final List<TabClosedEventHandler> handlerList = new ArrayList<>();
    private final EditorDragListener listener;

    int dropTargetIndex = -1;
    int dropSourceIndex = -1;
    private int selectedTab;
    private TabAcceptor tabAcceptor = (component, index) -> true;

    /**
     * Create new Editor Tabbed Pane.
     */
    public EditorTabbedPane() {
        setUI(new CustomTabbedPaneUI(this));

        glassPane = new DraggingGlassPane(this);
        setFocusable(false); //Prevent focus dotted line from appearing
        super.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        addChangeListener(e -> {
            firePropertyChange(SELECTED_TAB_PROPERTY, selectedTab, getSelectedIndex());
            selectedTab = getSelectedIndex();
        });
        listener = new EditorDragListener(this);
        var dropTargetListener = new CDropTargetListener(this);
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener, true);
        new DragSource().createDefaultDragGestureRecognizer(this,
                                                            DnDConstants.ACTION_COPY_OR_MOVE,
                                                            listener);
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

    @Override
    public void paint(final Graphics g) {
        getUI().paint(g, this);
        super.paint(g);
    }

    @Override
    public void setTabLayoutPolicy(final int tabLayoutPolicy) {
        // Only top tabs are supported at the moment.
    }

    @NotNull
    public Rectangle getTabAreaBound() {
        final Rectangle firstTab = getUI().getTabBounds(this, 0);
        return new Rectangle(0, 0, getWidth(), firstTab.y + firstTab.height);
    }

    /**
     * returns potential index for drop.
     *
     * @param point point given in the drop site component's coordinate
     * @return returns potential index for drop.
     */
    private int getTargetTabIndex(final Point point) {
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

    /*default*/ Point buildGhostLocation(final Point location) {
        Point ghostLocation = new Point(location);
        ghostLocation.y = 0;
        ghostLocation.x -= glassPane.getGhostWidth() / 2;
        ghostLocation = SwingUtilities
                .convertPoint(EditorTabbedPane.this, ghostLocation, glassPane);
        ghostLocation.x = Math.min(Math.max(getX(), ghostLocation.x),
                                   getX() + getWidth() - glassPane.getGhostWidth());
        ghostLocation.y = Math.min(Math.max(getY(), ghostLocation.y),
                                   getY() + getHeight() - glassPane.getGhostHeight());
        return ghostLocation;
    }

    /*default*/ void initGlassPane(@NotNull final Component c,
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

    /*default*/ void initTarget(final Point location) {
        final int newLocation = getTargetTabIndex(location);
        if (newLocation < 0 && listener.isDragging()) {
            return;
        }
        dropTargetIndex = newLocation;
    }
}
