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
                    var ancestor = SwingUtilities.getWindowAncestor(PersistableComponent.this);
                    if (ancestor != null) {
                        permManager.updateState(PersistableComponent.this, ancestor.getName());
                        removeAncestorListener(this);
                    }
                }
            });
        } else {
            permManager.updateState(this, ancestor.getName());
        }
    }
}
