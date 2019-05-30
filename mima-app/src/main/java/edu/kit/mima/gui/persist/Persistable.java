package edu.kit.mima.gui.persist;

import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2019
 */
public interface Persistable<T> {

    PersistenceInfo saveState();

    void loadState(final PersistenceInfo info);

    Set<?> getKeys();

    String getIdentifier();

    boolean isPersistable();

    void setPersistable(final boolean persistable, final String identifier);
}
