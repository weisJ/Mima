package edu.kit.mima.gui.components;

import edu.kit.mima.gui.laf.icons.Icons;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Breakpoint extends IndexComponent {

    public Breakpoint(int lineIndex) {
        int index = lineIndex;
        IconPanel iconPanel = new IconPanel(Icons.BREAKPOINT, Alignment.NORTH_WEST);
        setPreferredSize(iconPanel.getPreferredSize());
        add(iconPanel);
    }
}
