package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Persistence Info Object.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class PersistenceInfo {

    private final Map<String, Object> values;

    @Contract(pure = true)
    public PersistenceInfo() {
        values = new HashMap<>();
    }

    /**
     * Store a value.
     *
     * @param key   the key.
     * @param value the value.
     */
    public void putValue(final String key, final Object value) {
        values.put(key, value != null ? value.toString() : null);
    }

    /**
     * Get the value associated with the key.
     *
     * @param key          the kay.
     * @param defaultValue default value to use when value could not be loaded.
     * @return the value.
     */
    public String getValue(final String key, final String defaultValue) {
        String value = (String) values.get(key);
        return value != null ? value : defaultValue;
    }

    public double getDouble(final String key, final double defaultValue) {
        String value = (String) values.get(key);
        try {
            return Double.parseDouble(value);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getInt(final String key, final int defaultValue) {
        String value = (String) values.get(key);
        try {
            return Integer.parseInt(value);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        String value = (String) values.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public void put(@NotNull final PersistenceInfo saveState) {
        this.values.putAll(saveState.values);
    }

    public void put(@NotNull final PersistenceInfo saveState, final String prefix) {
        for (var key : saveState.values.keySet()) {
            values.put(prefix + '.' + key, saveState.values.get(key));
        }
    }

    public Set<String> getKeys() {
        return values.keySet();
    }
}
