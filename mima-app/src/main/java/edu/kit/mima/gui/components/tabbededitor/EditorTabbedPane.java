package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.api.event.SubscriptionManager;
import edu.kit.mima.api.event.SubscriptionService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

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
    private static final SubscriptionService<Integer> SUBSCRIPTION_SERVICE =
            new SubscriptionService<>(EditorTabbedPane.class);

    static {
        SubscriptionManager.getCurrentManager()
                .offerSubscription(SUBSCRIPTION_SERVICE, SELECTED_TAB_PROPERTY);
    }

    private final EditorDragSupport dragSupport;

    private final List<TabClosedEventHandler> handlerList = new ArrayList<>();
    private int selectedTab;
    private TabAcceptor tabAcceptor = (component, index) -> true;

    /**
     * Create new Editor Tabbed Pane.
     */
    public EditorTabbedPane() {
        setFocusable(false); // Prevent focus dotted line from appearing
        super.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        addChangeListener(
                e -> {
                    firePropertyChange(SELECTED_TAB_PROPERTY, selectedTab, getSelectedIndex());
                    SUBSCRIPTION_SERVICE.notifyEvent(
                            SELECTED_TAB_PROPERTY, getSelectedIndex(), EditorTabbedPane.this);
                    selectedTab = getSelectedIndex();
                });
        dragSupport = new EditorDragSupport(this);
    }

    @NotNull
    @Override
    public String getUIClassID() {
        return "EditorTabbedPaneUI";
    }

    @Override
    public void insertTab(
            final String title,
            final Icon icon,
            final Component component,
            final String tip,
            final int index) {
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
     * Get the EditorDragSupport.
     *
     * @return the drag support.
     */
    public EditorDragSupport getDragSupport() {
        return dragSupport;
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
    public void setTabLayoutPolicy(final int tabLayoutPolicy) {
        // Only top tabs are supported at the moment.
    }

    @NotNull
    public Rectangle getTabAreaBound() {
        final Rectangle firstTab = getUI().getTabBounds(this, 0);
        return new Rectangle(0, 0, getWidth(), firstTab.y + firstTab.height);
    }
}
