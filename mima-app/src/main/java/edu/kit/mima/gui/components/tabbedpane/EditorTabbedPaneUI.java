package edu.kit.mima.gui.components.tabbedpane;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Custom UI for {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class EditorTabbedPaneUI extends DnDTabbedPaneUI {

    protected abstract void setupColors();

    protected void drawTab(@NotNull final Graphics2D g, final int index, final boolean isSelected) {
        super.drawTab(g, index, isSelected);
        final var bounds = rects[index];
        final int yOff = bounds.height / 8;
        if (isSelected) {
            g.setColor(selectedColor);
            g.fillRect(bounds.x, bounds.y + bounds.height - yOff + 1, bounds.width - 1, yOff);
        }
        g.setColor(tabBorderColor);
        g.fillRect(bounds.x + bounds.width - 1, bounds.y, 1, bounds.height);
    }
}
