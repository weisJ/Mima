package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;

import java.awt.Dimension;
import java.awt.Font;

/**
 * Preview for {@link Console}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ConsolePreview extends AbstractPreviewPane {

    private Console console;

    /**
     * Create new Console Preview.
     */
    public ConsolePreview() {
        console = new Console();
        console.print("Test Test Console");
        console.setEnabled(false);
        add(console);
    }

    @Override
    public void setPreviewFont(final Font previewFont) {
        console.setConsoleFont(previewFont);
    }

    @Override
    public void setDimension(final Dimension dimension) {
        console.setPreferredSize(dimension);
        setPreferredSize(dimension);
    }
}
