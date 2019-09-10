package edu.kit.mima.gui.persist;

/**
 * Object that can be persisted across sessions.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface Persistable {

    /**
     * Create the persistence info of the object as a snapshot.
     *
     * @return the persistence info.
     */
    PersistenceInfo saveState();

    /**
     * Load the state from given persistence info.
     *
     * @param info the info to load.
     */
    void loadState(final PersistenceInfo info);

    /**
     * Get the identifier for the object.
     *
     * @return the identifier.
     */
    String getIdentifier();

    /**
     * Returns whether the given object is enabled for persisting.
     *
     * @return true if enabled.
     */
    boolean isPersistable();

    /**
     * Set whether the object should be persisted.
     *
     * @param persistable true if it should be persisted.
     * @param identifier  the identifier to use.
     */
    void setPersistable(final boolean persistable, final String identifier);
}
