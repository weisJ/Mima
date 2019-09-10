package edu.kit.mima.gui.components;

import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.gui.components.text.numberedpane.NumberedTextPane;

import javax.swing.*;
import java.awt.*;

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
        updateUI();
    }

    @Override
    public void paint(final Graphics g) {
        Icons.BREAKPOINT.paintIcon(this, g, 0, 0);
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Icons.BREAKPOINT.getIconWidth(), Icons.BREAKPOINT.getIconHeight());
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
