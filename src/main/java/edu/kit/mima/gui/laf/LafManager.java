package edu.kit.mima.gui.laf;

import edu.kit.mima.App;
import edu.kit.mima.gui.util.HSLColor;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class LafManager implements UserPreferenceChangedListener {

    private static final String DARK_NAME = "Dark";
    private static final String LIGHT_NAME = "Light";
    private static LafManager instance = new LafManager();
    private static String currentLaf;

    static {
        Preferences.registerUserPreferenceChangedListener(instance);
    }

    private LafManager() { }

    public static String getCurrentLaf() {
        return currentLaf;
    }

    private static void setDefaultTheme(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(CustomDarculaLaf.class.getCanonicalName());
            } else {
                UIManager.setLookAndFeel(CustomDarculaLightLaf.class.getCanonicalName());
            }
            updateLAF();
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void setTheme(String loaf) {
        try {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            UIManager.setLookAndFeel(loaf);
            updateLAF();
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static void updateLAF() {
        installFixes();
        if (App.isInitialized()) {
            for (Frame f : Frame.getFrames()) {
                updateLAFRecursively(f);
            }
        }
    }

    private static void updateLAFRecursively(Window window) {
        for (Window childWindow : window.getOwnedWindows()) {
            updateLAFRecursively(childWindow);
        }
        SwingUtilities.updateComponentTreeUI(window);
    }

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
        UIManager.put("TabbedPane.separaterHighlight", UIManager.getColor("TabbedPane.selected"));
        UIManager.put("TabbedPane.selected",
                new HSLColor(UIManager.getColor("TabbedPane.background")).adjustTone(20));
        UIManager.put("swing.boldMetal", Boolean.FALSE);
    }

    @Override
    public void notifyUserPreferenceChanged(PropertyKey key) {
        if (key == PropertyKey.THEME_PATH) {
            var pref = Preferences.getInstance();
            String name = pref.readString(PropertyKey.THEME);
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
