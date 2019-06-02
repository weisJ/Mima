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
    private final Map<String, Map<String, Persistable<?>>> persistableViews;
    private final Properties states;
    private final PersistenceInfo stateInfo;


    @Contract(pure = true)
    private PersistenceManager() {
        persistableViews = new HashMap<>();
        states = new Properties();
        stateInfo = new PersistenceInfo();
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
        for (var entry : states.entrySet()) {
            stateInfo.putValue(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Register a component to save its state.
     *
     * @param persistable the persistable component to save.
     * @param frameIdentifier identifier of parent frame.
     */
    public void registerState(@NotNull final Persistable<?> persistable, final String frameIdentifier) {
        if (persistable.isPersistable()) {
            if (!persistableViews.containsKey(frameIdentifier)) {
                persistableViews.put(frameIdentifier, new HashMap<>());
            }
            persistableViews.get(frameIdentifier).put(persistable.getIdentifier(), persistable);
        }
    }

    /**
     * Stop a component from persisting its state.
     *
     * @param persistable the persistable component to remove.
     * @param frameIdentifier identifier of parent frame.
     */
    public void removeState(@NotNull final Persistable<?> persistable, final String frameIdentifier) {
        if (persistableViews.containsKey(frameIdentifier)) {
            persistableViews.get(frameIdentifier).remove(persistable.getIdentifier());
        }
        stateInfo.remove(persistable.saveState(), persistable.getIdentifier());
    }

    /**
     * Update the persistence state based on the {@link Persistable#isPersistable()} value.
     *
     * @param persistable     the component to persist.
     * @param frameIdentifier the frame identifier.
     */
    public void updateState(@NotNull final Persistable<?> persistable, final String frameIdentifier) {
        if (persistable.isPersistable()) {
            registerState(persistable, frameIdentifier);
        } else {
            removeState(persistable, frameIdentifier);
        }
    }

    /**
     * Save all states.
     */
    public void saveStates() {
        for (var permMap : persistableViews.entrySet()) {
            for (var key : permMap.getValue().keySet()) {
                var permObj = permMap.getValue().get(key);
                if (permObj.isPersistable()) {
                    stateInfo.merge(permObj.saveState(), permMap.getKey() + '.' + key);
                }
            }
        }
        states.putAll(stateInfo.directMap());
        try {
            states.store(new FileOutputStream(optionsPath), "Mima Session");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the states for all frames.
     */
    public void loadStates() {
        for (var frame : persistableViews.keySet()) {
            loadStates(frame);
        }
    }

    /**
     * Load the state of specified frame.
     *
     * @param frameIdentifier the frame identifier.
     */
    public void loadStates(final String frameIdentifier) {
        if (persistableViews.containsKey(frameIdentifier)) {
            for (var perm : persistableViews.get(frameIdentifier).values()) {
                perm.loadState(getStates(perm, frameIdentifier));
            }
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
        String key = prefix.isEmpty() ? persistable.getIdentifier() : prefix + '.' + persistable.getIdentifier();
        return stateInfo.getSubTree(key);
    }
}
