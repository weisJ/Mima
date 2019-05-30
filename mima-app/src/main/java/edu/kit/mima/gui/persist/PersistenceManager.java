package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manager for controlling the persistence of the UI view.
 *
 * @author Jannis Weis
 * @since 2019
 */
public final class PersistenceManager {

    private static final String directory = System.getProperty("user.home") + "\\.mima";
    private static final String optionsPath = directory + "\\session_view.properties";
    private static final PersistenceManager instance = new PersistenceManager();
    private final Map<String, Persistable<?>> persistableViews;
    private final Properties states;


    @Contract(pure = true)
    private PersistenceManager() {
        persistableViews = new HashMap<>();
        states = new Properties();
        loadState();
    }

    @Contract(pure = true)
    public static PersistenceManager getInstance() {
        return instance;
    }

    private void loadState() {
        final File optionsFile = new File(optionsPath);
        try {
            if (!optionsFile.exists()) {
                final File dir = new File(directory);
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                states.store(new FileOutputStream(optionsPath), "Mima Session");
            } else {
                states.load(new FileInputStream(optionsFile));
            }
        } catch (@NotNull final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a component to save its state.
     *
     * @param persistable the persistable component to save.
     */
    public void registerState(@NotNull final Persistable<?> persistable) {
        if (persistable.isPersistable()) {
            persistableViews.put(persistable.getIdentifier(), persistable);
        }
    }

    /**
     * Stop a component from persisting its state.
     *
     * @param persistable the persistable component to remove.
     */
    public void removeState(@NotNull final Persistable<?> persistable) {
        persistableViews.remove(persistable.getIdentifier());
        for (var key : persistable.getKeys()) {
            var k = persistable.getIdentifier() + '.' + key;
            states.remove(k);
        }
    }

    public void updateState(@NotNull final Persistable<?> persistable) {
        if (persistable.isPersistable()) {
            registerState(persistable);
        } else {
            removeState(persistable);
        }
    }

    /**
     * Save all states.
     */
    public void saveStates() {
        for (var key : persistableViews.keySet()) {
            var permObj = persistableViews.get(key);
            if (permObj.isPersistable()) {
                var info = permObj.saveState();
                for (var k : info.getKeys()) {
                    states.put(key + '.' + k, info.getValue(k, ""));
                }
            }
        }
        try {
            states.store(new FileOutputStream(optionsPath), "Mima Session");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the states.
     */
    public void loadStates() {
        for (var perm : persistableViews.values()) {
            perm.loadState(getStates(perm));
        }
    }

    /**
     * Get the state for a specific component.
     *
     * @param persistable the persistable component.
     * @param <T>         the type of the component.
     * @return the persistence info for the component.
     */
    public <T> PersistenceInfo getStates(@NotNull final Persistable<T> persistable) {
        return getStates(persistable, "");
    }

    /**
     * Get the state for a specific component.
     *
     * @param persistable the persistable component.
     * @param prefix      prefix to load states.
     * @param <T>         the type of the component.
     * @return the persistence info for the component.
     */
    public <T> PersistenceInfo getStates(@NotNull final Persistable<T> persistable, @NotNull final String prefix) {
        PersistenceInfo info = new PersistenceInfo();
        var pref = prefix.isEmpty() ? "" : prefix + '.';
        for (var key : persistable.getKeys()) {
            var k = pref + persistable.getIdentifier() + '.' + key;
            info.putValue(key.toString(), states.get(k));
        }
        return info;
    }
}
