package edu.kit.mima.gui.laf.components;

import edu.kit.mima.api.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Graphics;

/**
 * Darcula UI for {@link edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane}.
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
    public static ComponentUI createUI(JComponent c) {
        return new LightEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("EditorTabbedPane.selectedTab");
        tabBorderColor = UIManager.getColor("Border.line2");
        selectedBackground = UIManager.getColor("EditorTabbedPane.selectedTab");
        dropColor = UIManager.getColor("EditorTabbedPane.dropColor");
    }

    @Override
    protected void paintTabArea(@NotNull Graphics graphics, int tabPlacement, int selectedIndex) {
        super.paintTabArea(graphics, tabPlacement, selectedIndex);
        if (selectedIndex >= 0) {
            var bounds = tabbedPane.getTabAreaBound();
            var selectedBounds = tabbedPane.getBoundsAt(selectedIndex);
            int height = bounds.height / 5;
            int yOff = bounds.height - height;
            graphics.setColor(selectedBackground);
            graphics.fillRect(0, yOff, bounds.width - 1, height);
            graphics.setColor(tabBorderColor);
            graphics.drawLine(-1, yOff, selectedBounds.x - 1, yOff);
            graphics.drawLine(selectedBounds.x + selectedBounds.width - 1, yOff, bounds.width,
                              yOff);
            graphics.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);

        }
    }
}
