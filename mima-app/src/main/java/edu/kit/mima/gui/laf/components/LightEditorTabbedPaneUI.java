package edu.kit.mima.gui.laf.components;

import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPaneUI;
import edu.kit.mima.util.HSLColor;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;

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
    public static ComponentUI createUI(JComponent c) {
        return new LightEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        final var c = UIManager.getColor("TabbedPane.separatorHighlight");
        final Color lineColor = c == null ? UIManager.getColor("TabbedPane.selected") : c;
        selectedColor = UIManager.getColor("EditorTabbedPane.selectionAccent");
        tabBorderColor = UIManager.getColor("Border.light");
        selectedBackground = new HSLColor(UIManager.getColor("TabbedPane.background"))
                .adjustTone(20).adjustSaturation(5).getRGB();
        dropColor = new HSLColor(UIManager.getColor("TabbedPane.background"))
                .adjustTone(15).adjustHue(20).adjustSaturation(0.2f).getRGB();
    }
}
