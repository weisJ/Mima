package edu.kit.mima.preferences;

import java.awt.Color;
import java.awt.Font;
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
 * @author Jannis Weis
 * @since 2018
 */
public class Preferences {

    private static final String directory = System.getProperty("user.home") + "\\.mima";
    private static final String optionsPath = directory + "\\options.properties";
    private static final Preferences instance = new Preferences();
    private static final List<UserPreferenceChangedListener> listenerList = new ArrayList<>();
    private static boolean notify = false;
    private final Properties options;
    private final Properties colorStyle;

    private Preferences() {
        options = new Properties();
        colorStyle = new Properties();
        File optionsFile = new File(optionsPath);
        if (!optionsFile.exists()) {
            createOptions();
        } else {
            try {
                options.load(new FileInputStream(optionsFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadTheme(readString(PropertyKey.THEME_EDITOR));
        Preferences.notify = true;
    }

    public static Preferences getInstance() {
        return instance;
    }

    public static void registerUserPreferenceChangedListener(UserPreferenceChangedListener listener) {
        Preferences.listenerList.add(listener);
    }

    public static boolean removeUserPreferenceChangedListener(UserPreferenceChangedListener listener) {
        return Preferences.listenerList.remove(listener);
    }

    private static void notifyListeners(PropertyKey key) {
        if (!Preferences.notify) {
            return;
        }
        for (var listener : listenerList) {
            if (listener != null) {
                listener.notifyUserPreferenceChanged(key);
            }
        }
    }

    public void saveOptions() throws IOException {
        File directory = new File(Preferences.directory);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        options.store(new FileOutputStream(optionsPath), "Mima Options");
    }

    private void createOptions() {
        try (final InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("options.properties"))) {
            options.load(inputStream);
            saveString(PropertyKey.DIRECTORY_MIMA, directory);
            saveOptions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTheme(String name) {
        try (final InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(name + ".properties"))) {
            colorStyle.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveBoolean(PropertyKey key, boolean value) {
        saveValue(PropertyType.BOOLEAN, key, String.valueOf(value));
    }

    public void saveInteger(PropertyKey key, int value) {
        saveValue(PropertyType.INTEGER, key, String.valueOf(value));
    }

    public void saveString(PropertyKey key, String value) {
        saveValue(PropertyType.STRING, key, value);
    }

    private void saveValue(PropertyType type, PropertyKey key, String value) {
        if (key.getType() != type) {
            throw new PreferenceException("Can't save " + type.toString() + " to " + key.toString());
        }
        options.setProperty(key.toString(), value);
        notifyListeners(key);
    }

    public boolean readBoolean(PropertyKey key) {
        return Boolean.parseBoolean(options.getProperty(key.toString()));
    }

    public int readInteger(PropertyKey key) {
        return Integer.parseInt(options.getProperty(key.toString()));
    }

    public String readString(PropertyKey key) {
        return options.getProperty(key.toString());
    }

    public Color readColor(ColorKey key) {
        return new Color(Integer.parseInt(colorStyle.getProperty(key.toString()), 16));
    }

    public Font readFont(PropertyKey key) {
        try {
            String[] decode = options.getProperty(key.toString()).split("-");
            //noinspection MagicConstant
            return new Font(decode[0], Integer.parseInt(decode[1]), Integer.parseInt(decode[2]));
        } catch (Exception e) {
            return new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    public void saveFont(PropertyKey key, Font value) {
        String v = value.getFontName() + "-" + value.getStyle() + "-" + value.getSize();
        saveValue(PropertyType.Font, key, v);
    }
}
