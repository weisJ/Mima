package edu.kit.mima.preferences;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface UserPreferenceChangedListener {

    void notifyUserPreferenceChanged(PropertyKey key);
}
