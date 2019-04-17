package edu.kit.mima.gui.components;

import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.gui.components.numberedpane.NumberedTextPane;
import edu.kit.mima.gui.icons.Icons;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * BreakpointComponent to display in {@link NumberedTextPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BreakpointComponent extends IndexComponent implements Breakpoint {

    private final int lineIndex;
    private Color color;

    /**
     * Create BreakpointComponent at given line.
     *
     * @param lineIndex index xof line
     */
    public BreakpointComponent(final int lineIndex) {
        this.lineIndex = lineIndex;
        final IconPanel iconPanel = new IconPanel(Icons.BREAKPOINT);
        setPreferredSize(iconPanel.getPreferredSize());
        add(iconPanel);
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        color = UIManager.getColor("Editor.breakpoint");
    }

    @Override
    public Color getLineColor() {
        return color;
    }

    /**
     * Get the line index.
     *
     * @return line index.
     */
    public int getLineIndex() {
        return lineIndex;
    }
}
