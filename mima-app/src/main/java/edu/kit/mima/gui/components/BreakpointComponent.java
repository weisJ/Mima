package edu.kit.mima.gui.components;

import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.gui.laf.icons.Icons;

/**
 * BreakpointComponent to display in {@link NumberedTextPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BreakpointComponent extends IndexComponent implements Breakpoint {

    private final int lineIndex;

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
