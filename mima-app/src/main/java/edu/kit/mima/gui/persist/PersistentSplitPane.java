package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * {@link JSplitPane} that has persistable divider location.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class PersistentSplitPane extends JSplitPane implements Persistable {

    private final PersistenceInfo persistenceInfo;
    private String identifier;
    private boolean persistable;
    private double proportionalLocation;
    private boolean proportionalLocationSet = false;

    /**
     * Create new SplitPane that has a persistable divider location.
     */
    public PersistentSplitPane() {
        persistenceInfo = new PersistenceInfo();
        identifier = "";
        persistable = false;
    }

    @Override
    public void setDividerLocation(final double proportionalLocation) {
        super.setDividerLocation(proportionalLocation);
        this.proportionalLocation = proportionalLocation;
        this.proportionalLocationSet = true;
    }

    @Override
    public void paint(final Graphics g) {
        if (proportionalLocationSet) {
            setDividerLocation(proportionalLocation);
            proportionalLocationSet = false;
        }
        super.paint(g);

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
        pos = Math.max(0.0, Math.min(1.0, pos));
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
        PersistableComponent.updateInFuture(this);
    }
}
