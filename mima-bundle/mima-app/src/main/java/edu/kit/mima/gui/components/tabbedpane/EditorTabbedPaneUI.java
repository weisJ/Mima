package edu.kit.mima.gui.components.tabbedpane;

import org.jetbrains.annotations.NotNull;

import javax.swing.FocusManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;

/**
 * Custom UI for {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class EditorTabbedPaneUI extends DnDTabbedPaneUI {

    protected abstract void setupColors();

    private boolean hasFocus;

    private final AWTEventListener eventListener = new AWTEventListener() {
        @Override
        public void eventDispatched(final AWTEvent event) {
            var owner = FocusManager.getCurrentManager().getFocusOwner();
            if ((owner instanceof JRootPane && (SwingUtilities.isDescendingFrom(tabbedPane, owner)))
                 || owner == null) {
                return;
            }
            hasFocus = SwingUtilities.isDescendingFrom(owner, tabbedPane);
            tabbedPane.repaint();
        }
    };

    @Override
    protected void installListeners() {
        super.installListeners();
        Toolkit.getDefaultToolkit().addAWTEventListener(eventListener, AWTEvent.FOCUS_EVENT_MASK);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        Toolkit.getDefaultToolkit().removeAWTEventListener(eventListener);
    }

    protected void drawTab(@NotNull final Graphics2D g, final int index, final boolean isSelected) {
        super.drawTab(g, index, isSelected);
        final var bounds = rects[index];
        final int yOff = bounds.height / 8;
        if (isSelected) {
            g.setColor(hasFocus ? selectedColor : selectedUnfocusedColor);
            g.fillRect(bounds.x, bounds.y + bounds.height - yOff + 1, bounds.width - 1, yOff);
        }
        g.setColor(tabBorderColor);
        g.fillRect(bounds.x + bounds.width - 1, bounds.y, 1, bounds.height);
    }
}
