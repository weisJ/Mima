package edu.kit.mima.preferences;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Preference Loader/Saver for the Mima Application.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Preferences {

    public static final String DIR = System.getProperty("user.home") + "\\.mima";
    private static final String optionsPath = DIR + "\\options.properties";
    private static Preferences instance;
    private static final List<UserPreferenceChangedListener> listenerList = new ArrayList<>();
    @NotNull
    private final Properties options;
    @NotNull
    private final Properties colorStyle;
    private final boolean notify;

    private Preferences() {
        options = new Properties();
        colorStyle = new Properties();
        final File optionsFile = new File(optionsPath);
        if (!optionsFile.exists()) {
            createOptions();
        } else {
            try {
                options.load(new FileInputStream(optionsFile));
            } catch (@NotNull final IOException e) {
                e.printStackTrace();
            }
        }
        loadTheme(readString(PropertyKey.THEME_EDITOR));
        notify = true;
    }

    private Preferences(@NotNull final Properties options, @NotNull final Properties colorStyle) {
        this.options = (Properties) options.clone();
        this.colorStyle = (Properties) colorStyle.clone();
        notify = true;
    }

    @NotNull
    public static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    public static void registerUserPreferenceChangedListener(
            final UserPreferenceChangedListener listener) {
        Preferences.listenerList.add(listener);
    }

    public static boolean removeUserPreferenceChangedListener(
            final UserPreferenceChangedListener listener) {
        return Preferences.listenerList.remove(listener);
    }

    private void notifyListeners(final PropertyKey key) {
        if (!notify) {
            return;
        }
        for (final var listener : listenerList) {
            if (listener != null) {
                listener.notifyUserPreferenceChanged(key);
            }
        }
    }

    @NotNull
    @Contract(" -> new")
    @Override
    public Preferences clone() {
        return new Preferences(options, colorStyle);
    }

    /**
     * Save the Preferences.
     *
     * @throws IOException If options could not be saved.
     */
    public void saveOptions() throws IOException {
        final File directory = new File(Preferences.DIR);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        options.store(new FileOutputStream(optionsPath), "Mima Options");
    }

    private void createOptions() {
        try (final InputStream inputStream =
                     Objects.requireNonNull(
                             getClass().getClassLoader().getResourceAsStream("options.properties"))) {
            options.load(inputStream);
            saveString(PropertyKey.DIRECTORY_MIMA, DIR);
            saveOptions();
        } catch (@NotNull final IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTheme(final String name) {
        try (final InputStream inputStream =
                     Objects.requireNonNull(
                             getClass().getClassLoader().getResourceAsStream(name + ".properties"))) {
            colorStyle.load(inputStream);
        } catch (@NotNull final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save a boolean value.
     *
     * @param key   key to save to.
     * @param value value to save.
     */
    public void saveBoolean(@NotNull final PropertyKey key, final boolean value) {
        saveValue(PropertyType.BOOLEAN, key, String.valueOf(value));
    }

    /**
     * Save an integer value.
     *
     * @param key   key to save to.
     * @param value value to save.
     */
    public void saveInteger(@NotNull final PropertyKey key, final int value) {
        saveValue(PropertyType.INTEGER, key, String.valueOf(value));
    }

    /**
     * Save an string value.
     *
     * @param key   key to save to.
     * @param value value to save.
     */
    public void saveString(@NotNull final PropertyKey key, final String value) {
        saveValue(PropertyType.STRING, key, value);
    }

    private void saveValue(
            @NotNull final PropertyType type, @NotNull final PropertyKey key, final String value) {
        if (key.getType() != type) {
            throw new PreferenceException("Can't save " + type.toString() + " to " + key.toString());
        }
        options.setProperty(key.toString(), value);
        notifyListeners(key);
    }

    /**
     * Save a font value.
     *
     * @param key   key to save to.
     * @param value value to save.
     */
    public void saveFont(@NotNull final PropertyKey key, @NotNull final Font value) {
        final String v = value.getFontName() + "-" + value.getStyle() + "-" + value.getSize();
        saveValue(PropertyType.Font, key, v);
    }

    /**
     * Read a boolean value.
     *
     * @param key key to read.
     * @return value at key
     */
    public boolean readBoolean(@NotNull final PropertyKey key) {
        return Boolean.parseBoolean(options.getProperty(key.toString()));
    }

    /**
     * Read an integer value.
     *
     * @param key key to read.
     * @return value at key
     */
    public int readInteger(@NotNull final PropertyKey key) {
        return Integer.parseInt(options.getProperty(key.toString()));
    }

    /**
     * Read a string value.
     *
     * @param key key to read.
     * @return value at key
     */
    public String readString(@NotNull final PropertyKey key) {
        return options.getProperty(key.toString());
    }

    /**
     * Read a colour value.
     *
     * @param key key to read.
     * @return value at key
     */
    @Contract("_ -> new")
    @NotNull
    public Color readColor(@NotNull final ColorKey key) {
        return new Color(Integer.parseInt(colorStyle.getProperty(key.toString()), 16));
    }

    /**
     * Read a font value.
     *
     * @param key key to read.
     * @return value at key
     */
    @Contract("_ -> new")
    @NotNull
    public Font readFont(@NotNull final PropertyKey key) {
        try {
            final String[] decode = options.getProperty(key.toString()).split("-");
            //noinspection MagicConstant
            return new Font(decode[0], Integer.parseInt(decode[1]), Integer.parseInt(decode[2]));
        } catch (@NotNull final Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, 12);
        }
    }

    /**
     * Save existing preferences to this one.
     *
     * @param pref preferences to save.
     */
    public void save(@NotNull final Preferences pref) {
        for (var key : PropertyKey.values()) {
            saveValue(key.getType(), key, pref.options.getProperty(key.toString()));
        }
    }
}
