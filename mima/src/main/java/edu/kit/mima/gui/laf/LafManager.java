package edu.kit.mima.gui.laf;

import edu.kit.mima.App;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Manager for the Look and Feel.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class LafManager implements UserPreferenceChangedListener {

    private static final String DARK_NAME = "Dark";
    private static final String LIGHT_NAME = "Light";
    @NotNull private static final LafManager instance = new LafManager();
    private static String currentLaf;

    static {
        Preferences.registerUserPreferenceChangedListener(instance);
    }

    private LafManager() { }

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
            } else {
                UIManager.setLookAndFeel(CustomDarculaLightLaf.class.getCanonicalName());
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
        installFixes();
        if (App.isInitialized()) {
            for (final Frame f : Frame.getFrames()) {
                updateLafRecursively(f);
            }
        }
    }

    private static void updateLafRecursively(final Window window) {
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

    private static void installFixes() {
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("ToolTip.background", UIManager.getColor("TabbedPane.background"));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);
        UIManager.put("TabbedPane.labelShift", 0);
        UIManager.put("TabbedPane.selectedLabelShift", 0);
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.separatorHighlight", UIManager.getColor("TabbedPane.selected"));
        UIManager.put("TabbedPane.selected",
                      new HSLColor(UIManager.getColor("TabbedPane.background"))
                              .adjustTone(20).getRGB());
        UIManager.put("Border.light",
                      new HSLColor(UIManager.getColor("TabbedPane.background"))
                              .adjustTone(30).getRGB());
        UIManager.put("Button.separator", new ColorUIResource(95, 95, 95));
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }

    @Override
    public void notifyUserPreferenceChanged(final PropertyKey key) {
        if (key == PropertyKey.THEME_PATH) {
            final var pref = Preferences.getInstance();
            final String name = pref.readString(PropertyKey.THEME);
            currentLaf = name;
            if (name.equals(DARK_NAME)) {
                setDefaultTheme(true);
            } else if (name.equals(LIGHT_NAME)) {
                setDefaultTheme(false);
            } else {
                setTheme(pref.readString(PropertyKey.THEME_PATH));
            }
        }
    }
}
