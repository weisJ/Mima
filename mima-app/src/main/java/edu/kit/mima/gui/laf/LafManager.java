package edu.kit.mima.gui.laf;

import edu.kit.mima.App;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Frame;
import java.awt.Window;

/**
 * Manager for the Look and Feel.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class LafManager implements UserPreferenceChangedListener {

    public static final String DARK = "Dark";
    public static final String LIGHT = "Light";
    private static final LafManager instance = new LafManager();
    private static String currentLaf;


    private LafManager() {
        Preferences.registerUserPreferenceChangedListener(this);
    }

    @Contract(pure = true)
    public static String getCurrentLaf() {
        return currentLaf;
    }

    /**
     * Set the LaF to one of the two defaults.
     *
     * @param dark true if dark false if light.
     */
    public static void setDefaultTheme(final boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(CustomDarculaLaf.class.getCanonicalName());
                currentLaf = DARK;
            } else {
                UIManager.setLookAndFeel(CustomDarculaLightLaf.class.getCanonicalName());
                currentLaf = LIGHT;
            }
            updateLaf();
        } catch (@NotNull final ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void setTheme(final String loaf) {
        try {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.setLookAndFeel(loaf);
            updateLaf();
        } catch (@NotNull final ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void updateLaf() {
        if (App.isInitialized()) {
            for (final Frame f : Frame.getFrames()) {
                updateLafRecursively(f);
            }
        }
    }

    private static void updateLafRecursively(@NotNull final Window window) {
        for (final Window childWindow : window.getOwnedWindows()) {
            updateLafRecursively(childWindow);
        }
        SwingUtilities.updateComponentTreeUI(window);
    }

    /**
     * Update the LaF.
     */
    public static void update() {
        instance.notifyUserPreferenceChanged(PropertyKey.THEME_PATH);
    }

    @Override
    public void notifyUserPreferenceChanged(final PropertyKey key) {
        if (key == PropertyKey.THEME_PATH) {
            final var pref = Preferences.getInstance();
            final String name = pref.readString(PropertyKey.THEME);
            currentLaf = name;
            if (name.equals(DARK)) {
                setDefaultTheme(true);
            } else if (name.equals(LIGHT)) {
                setDefaultTheme(false);
            } else {
                setTheme(pref.readString(PropertyKey.THEME_PATH));
            }
        }
    }
}
