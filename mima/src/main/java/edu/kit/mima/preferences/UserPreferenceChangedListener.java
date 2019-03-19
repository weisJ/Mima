package edu.kit.mima.preferences;

/**
 * Listener for events where the {@link Preferences} have changed.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface UserPreferenceChangedListener {

    void notifyUserPreferenceChanged(PropertyKey key);
}
