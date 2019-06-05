package edu.kit.mima.gui.laf.components;

import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.components.tabbedpane.EditorTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Darcula UI for {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LightEditorTabbedPaneUI extends EditorTabbedPaneUI {

    /**
     * Create a UI.
     *
     * @param c a component
     * @return a UI
     */
    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new LightEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("DnDTabbedPane.selectedTab");
        tabBorderColor = UIManager.getColor("Border.line2");
        selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab");
        dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
    }

    @Override
    protected void paintTabArea(@NotNull final Graphics g, final int tabPlacement, final int selectedIndex) {
        super.paintTabArea(g, tabPlacement, selectedIndex);
        if (selectedIndex >= 0) {
            var bounds = tabbedPane.getTabAreaBound();
            var selectedBounds = tabbedPane.getBoundsAt(selectedIndex);
            int height = bounds.height / 5;
            int yOff = bounds.height - height;
            g.setColor(selectedBackground);
            g.fillRect(0, yOff, bounds.width - 1, height);
            g.setColor(tabBorderColor);
            g.drawLine(-1, yOff, selectedBounds.x - 1, yOff);
            g.drawLine(selectedBounds.x + selectedBounds.width - 1, yOff, bounds.width, yOff);
            g.drawLine(0, bounds.height, bounds.width, bounds.height);
        }
    }
}
