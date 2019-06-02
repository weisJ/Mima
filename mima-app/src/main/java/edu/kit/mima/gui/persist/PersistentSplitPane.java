package edu.kit.mima.gui.persist;

import edu.kit.mima.gui.components.listeners.AncestorAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;

/**
 * {@link JSplitPane} that has persistable divider location.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class PersistentSplitPane extends JSplitPane implements Persistable<JSplitPane> {

    private final PersistenceInfo persistenceInfo;
    private String identifier;
    private boolean persistable;

    /**
     * Create new SplitPane that has a persistable divider location.
     */
    public PersistentSplitPane() {
        persistenceInfo = new PersistenceInfo();
        identifier = "";
        persistable = false;
    }

    @Override
    public PersistenceInfo saveState() {
        double pos = getRelativeDividerLocation();
        persistenceInfo.putValue("position", pos);
        return persistenceInfo;
    }

    /**
     * Get the relative divider location.
     *
     * @return the relative divider location.
     */
    public double getRelativeDividerLocation() {
        int dividerLocation = Math.max(getMinimumDividerLocation(),
                                       Math.min(getMaximumDividerLocation(), getDividerLocation()));
        return dividerLocation / (double) getOrientedSize();
    }

    private int getOrientedSize() {
        return getOrientation() == JSplitPane.VERTICAL_SPLIT
                       ? getHeight() - getDividerSize()
                       : getWidth() - getDividerSize();
    }

    @Override
    public void loadState(@NotNull final PersistenceInfo info) {
        double pos = info.getDouble("position", getRelativeDividerLocation());
        persistenceInfo.putValue("position", pos);
        setDividerLocation(pos);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isPersistable() {
        return persistable;
    }

    @Override
    public void setPersistable(final boolean persistable, final String identifier) {
        this.persistable = persistable;
        this.identifier = identifier;
        var ancestor = SwingUtilities.getWindowAncestor(this);
        var permManager = PersistenceManager.getInstance();
        if (ancestor == null) {
            addAncestorListener(new AncestorAdapter() {
                @Override
                public void ancestorAdded(final AncestorEvent event) {
                    var ancestor = SwingUtilities.getWindowAncestor(PersistentSplitPane.this);
                    if (ancestor != null) {
                        permManager.updateState(PersistentSplitPane.this, ancestor.getName());
                        removeAncestorListener(this);
                    }
                }
            });
        } else {
            permManager.updateState(this, ancestor.getName());
        }
    }
}
