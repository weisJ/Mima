package edu.kit.mima.gui.icons;

import edu.kit.mima.gui.laf.LafManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Icon that is aware of the current ui theme and adjusts the icon accordingly. Icons are loaded
 * lazily at their point of usage.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class UIAwareIcon implements Icon {

    private final UIAwareIcon dual;
    private final String darkKey;
    private final String lightKey;
    private boolean loaded;
    private String currentTheme;
    private Icon icon;

    /**
     * Create new ui aware icon.
     *
     * @param darkKey  key to load icon for dark mode.
     * @param lightKey key to load icon for light mode.
     */
    @Contract(pure = true)
    public UIAwareIcon(final String darkKey, final String lightKey) {
        this.darkKey = darkKey;
        this.lightKey = lightKey;
        this.dual = new UIAwareIcon(this);
    }

    @Contract(pure = true)
    private UIAwareIcon(@NotNull final UIAwareIcon dual) {
        this.darkKey = dual.lightKey;
        this.lightKey = dual.darkKey;
        this.dual = dual;

    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        ensureLoaded();
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        ensureLoaded();
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        ensureLoaded();
        return icon.getIconHeight();
    }

    private void ensureLoaded() {
        if (!isLoaded()) {
            loadIcon();
        }
    }

    @Contract(pure = true)
    private boolean isLoaded() {
        return loaded && LafManager.getCurrentLaf().equals(currentTheme);
    }

    private void loadIcon() {
        currentTheme = LafManager.getCurrentLaf();
        if (currentTheme.equals(LafManager.DARK)) {
            icon = Icons.loadIcon(darkKey);
        } else {
            icon = Icons.loadIcon(lightKey);
        }
        loaded = true;
    }

    public UIAwareIcon getDual() {
        return dual;
    }
}
