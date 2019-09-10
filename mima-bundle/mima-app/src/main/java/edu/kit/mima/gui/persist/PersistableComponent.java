package edu.kit.mima.gui.persist;

import edu.kit.mima.gui.components.listeners.AncestorAdapter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;

/**
 * Simple implementation for a JComponent that can be persisted.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class PersistableComponent extends JComponent implements Persistable {

    protected final PersistenceInfo persistenceInfo = new PersistenceInfo();
    protected String identifier;
    protected boolean persistable;

    /**
     * Update the persistence state of the component when it has a parent.
     *
     * @param persistable component to update.
     * @param <T>         type of component. Must be Persistable and JComponent.
     */
    public static <T extends JComponent & Persistable> void updateInFuture(final T persistable) {
        var ancestor = SwingUtilities.getWindowAncestor(persistable);
        var permManager = PersistenceManager.getInstance();
        if (ancestor == null) {
            persistable.addAncestorListener(new AncestorAdapter() {
                @Override
                public void ancestorAdded(final AncestorEvent event) {
                    var ancestor = SwingUtilities.getWindowAncestor(persistable);
                    if (ancestor != null) {
                        permManager.updateState(persistable, ancestor.getName());
                        persistable.removeAncestorListener(this);
                    }
                }
            });
        } else {
            permManager.updateState(persistable, ancestor.getName());
        }
    }

    @Override
    public void setPersistable(final boolean persistable, final String identifier) {
        this.persistable = persistable;
        this.identifier = identifier;
        updateInFuture(this);
    }
}
