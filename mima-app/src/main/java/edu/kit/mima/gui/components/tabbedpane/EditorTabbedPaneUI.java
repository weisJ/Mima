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
            g.fillRect(bounds.x, bounds.y + bounds.height - yOff + 1, bounds.width, yOff);
        }
        g.translate(-0.5, 0);
        g.setColor(tabBorderColor);
        g.drawLine(bounds.x + bounds.width, bounds.y,
                   bounds.x + bounds.width, bounds.y + bounds.height);
        g.translate(0.5, 0);
    }
}
