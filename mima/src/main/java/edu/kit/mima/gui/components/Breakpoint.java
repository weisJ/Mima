package edu.kit.mima.gui.components;

import edu.kit.mima.gui.laf.icons.Icons;

/**
 * Breakpoint to display in {@link NumberedTextPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Breakpoint extends IndexComponent {

    private final int lineIndex;

    /**
     * Create Breakpoint at given line.
     *
     * @param lineIndex index xof line
     */
    public Breakpoint(final int lineIndex) {
        this.lineIndex = lineIndex;
        final IconPanel iconPanel = new IconPanel(Icons.BREAKPOINT, Alignment.NORTH_WEST);
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
